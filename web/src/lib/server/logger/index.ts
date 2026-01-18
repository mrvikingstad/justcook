import pino from 'pino';
import { dev } from '$app/environment';
import { env } from '$env/dynamic/private';
import { AsyncLocalStorage } from 'async_hooks';

// AsyncLocalStorage for request context (correlation IDs)
export const requestContext = new AsyncLocalStorage<{ requestId: string }>();

// Configure pino based on environment
const transport = dev
	? {
			target: 'pino-pretty',
			options: {
				colorize: true,
				translateTime: 'SYS:standard',
				ignore: 'pid,hostname'
			}
		}
	: undefined;

const baseLogger = pino({
	level: env.LOG_LEVEL || (dev ? 'debug' : 'info'),
	transport,
	formatters: {
		level: (label) => ({ level: label })
	},
	base: {
		env: dev ? 'development' : 'production'
	}
});

/**
 * Get a child logger with request context (correlation ID)
 * Automatically includes requestId if running within a request context
 */
function getLogger() {
	const context = requestContext.getStore();
	if (context?.requestId) {
		return baseLogger.child({ requestId: context.requestId });
	}
	return baseLogger;
}

/**
 * Structured logger with automatic request context
 */
export const logger = {
	/**
	 * Debug level - detailed information for debugging
	 */
	debug: (msg: string, data?: Record<string, unknown>) => {
		getLogger().debug(data, msg);
	},

	/**
	 * Info level - general operational information
	 */
	info: (msg: string, data?: Record<string, unknown>) => {
		getLogger().info(data, msg);
	},

	/**
	 * Warn level - potentially harmful situations
	 */
	warn: (msg: string, data?: Record<string, unknown>) => {
		getLogger().warn(data, msg);
	},

	/**
	 * Error level - error events that might still allow the app to continue
	 */
	error: (msg: string, error?: unknown, data?: Record<string, unknown>) => {
		const errorData = error instanceof Error
			? { error: { message: error.message, stack: error.stack, name: error.name }, ...data }
			: { error, ...data };
		getLogger().error(errorData, msg);
	},

	/**
	 * Create a child logger with additional context
	 */
	child: (bindings: Record<string, unknown>) => {
		const childLogger = getLogger().child(bindings);
		return {
			debug: (msg: string, data?: Record<string, unknown>) => childLogger.debug(data, msg),
			info: (msg: string, data?: Record<string, unknown>) => childLogger.info(data, msg),
			warn: (msg: string, data?: Record<string, unknown>) => childLogger.warn(data, msg),
			error: (msg: string, error?: unknown, data?: Record<string, unknown>) => {
				const errorData = error instanceof Error
					? { error: { message: error.message, stack: error.stack, name: error.name }, ...data }
					: { error, ...data };
				childLogger.error(errorData, msg);
			}
		};
	}
};

/**
 * Generate a unique request ID
 */
export function generateRequestId(): string {
	return `req_${Date.now().toString(36)}_${Math.random().toString(36).substring(2, 9)}`;
}

/**
 * Get the current request ID from context
 * Returns undefined if not running within a request context
 */
export function getRequestId(): string | undefined {
	return requestContext.getStore()?.requestId;
}
