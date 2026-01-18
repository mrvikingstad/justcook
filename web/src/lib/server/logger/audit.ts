/**
 * Security Audit Logging
 *
 * Provides structured logging for security-sensitive events that require
 * an audit trail. These logs are critical for:
 * - Security incident investigation
 * - Compliance requirements
 * - Detecting brute force attacks
 * - Monitoring account takeover attempts
 *
 * All audit events include:
 * - Timestamp (automatic via pino)
 * - Event type (action)
 * - User identifier (userId, email, or IP)
 * - Outcome (success/failure)
 * - Additional context (IP, user agent, etc.)
 */

import { logger } from './index';

export type AuditAction =
	| 'auth.login.success'
	| 'auth.login.failed'
	| 'auth.login.rate_limited'
	| 'auth.logout'
	| 'auth.password.changed'
	| 'auth.password.reset_requested'
	| 'auth.password.reset_completed'
	| 'auth.email.verification_sent'
	| 'auth.email.verified'
	| 'account.created'
	| 'account.deleted'
	| 'account.username.changed'
	| 'account.email.changed'
	| 'session.invalidated'
	| 'session.invalidated_all';

export interface AuditContext {
	/** User ID if known */
	userId?: string;
	/** Email address for auth events */
	email?: string;
	/** Username if applicable */
	username?: string;
	/** Client IP address */
	ip?: string;
	/** User agent string */
	userAgent?: string;
	/** Request ID for correlation */
	requestId?: string;
	/** Additional event-specific data */
	metadata?: Record<string, unknown>;
}

/**
 * Log a security audit event
 *
 * @param action - The type of security event
 * @param outcome - Whether the action succeeded or failed
 * @param context - Additional context about the event
 *
 * @example
 * ```ts
 * audit('auth.login.failed', 'failure', {
 *   email: 'user@example.com',
 *   ip: '192.168.1.1',
 *   metadata: { reason: 'invalid_password' }
 * });
 * ```
 */
export function audit(
	action: AuditAction,
	outcome: 'success' | 'failure',
	context: AuditContext
): void {
	const auditLogger = logger.child({ audit: true });

	const logData = {
		action,
		outcome,
		userId: context.userId,
		email: context.email ? maskEmail(context.email) : undefined,
		username: context.username,
		ip: context.ip,
		userAgent: context.userAgent ? truncateUserAgent(context.userAgent) : undefined,
		requestId: context.requestId,
		...context.metadata
	};

	// Remove undefined values for cleaner logs
	const cleanedData = Object.fromEntries(
		Object.entries(logData).filter(([, v]) => v !== undefined)
	);

	// Use appropriate log level based on outcome and action type
	if (outcome === 'failure' && isHighRiskAction(action)) {
		auditLogger.warn(`[AUDIT] ${action}`, cleanedData);
	} else {
		auditLogger.info(`[AUDIT] ${action}`, cleanedData);
	}
}

/**
 * Log a successful authentication event
 */
export function auditAuthSuccess(
	action: Extract<AuditAction, `auth.${string}`>,
	context: AuditContext
): void {
	audit(action, 'success', context);
}

/**
 * Log a failed authentication event
 */
export function auditAuthFailure(
	action: Extract<AuditAction, `auth.${string}`>,
	context: AuditContext & { reason?: string }
): void {
	audit(action, 'failure', {
		...context,
		metadata: { ...context.metadata, reason: context.reason }
	});
}

/**
 * Log an account-related event
 */
export function auditAccount(
	action: Extract<AuditAction, `account.${string}`>,
	outcome: 'success' | 'failure',
	context: AuditContext
): void {
	audit(action, outcome, context);
}

/**
 * Log a session-related event
 */
export function auditSession(
	action: Extract<AuditAction, `session.${string}`>,
	context: AuditContext & { sessionsAffected?: number }
): void {
	audit(action, 'success', {
		...context,
		metadata: { ...context.metadata, sessionsAffected: context.sessionsAffected }
	});
}

/**
 * Determine if an action is high-risk (should be logged at warn level on failure)
 */
function isHighRiskAction(action: AuditAction): boolean {
	const highRiskActions: AuditAction[] = [
		'auth.login.failed',
		'auth.login.rate_limited',
		'auth.password.changed',
		'account.deleted',
		'session.invalidated_all'
	];
	return highRiskActions.includes(action);
}

/**
 * Mask email for privacy while keeping enough for identification
 * user@example.com -> u***@example.com
 */
function maskEmail(email: string): string {
	const [local, domain] = email.split('@');
	if (!local || !domain) return '***@***';
	if (local.length <= 2) return `${local[0]}***@${domain}`;
	return `${local[0]}***@${domain}`;
}

/**
 * Truncate user agent to reasonable length
 */
function truncateUserAgent(ua: string): string {
	const maxLength = 200;
	return ua.length > maxLength ? ua.substring(0, maxLength) + '...' : ua;
}
