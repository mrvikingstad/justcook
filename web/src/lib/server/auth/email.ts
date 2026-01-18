import { Resend } from 'resend';
import { RESEND_API_KEY } from '$env/static/private';
import { env } from '$env/dynamic/private';
import { dev } from '$app/environment';
import { logger } from '$lib/server/logger';

// Validate RESEND_API_KEY in production - emails are critical for auth flows
if (!RESEND_API_KEY) {
	if (dev) {
		logger.warn('RESEND_API_KEY is not set - emails will not be sent in development');
	} else {
		throw new Error('RESEND_API_KEY environment variable is required in production. Email verification and password reset will not work without it.');
	}
}

const resend = new Resend(RESEND_API_KEY);
const fromEmail = env.EMAIL_FROM || 'Just Cook <noreply@justcook.app>';

export async function sendMagicLinkEmail(email: string, url: string) {
	await resend.emails.send({
		from: fromEmail,
		to: email,
		subject: 'Sign in to Just Cook',
		html: `
			<!DOCTYPE html>
			<html>
				<head>
					<meta charset="utf-8">
					<meta name="viewport" content="width=device-width, initial-scale=1">
				</head>
				<body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f8f7f4; padding: 40px 20px; margin: 0;">
					<div style="max-width: 400px; margin: 0 auto; background: #ffffff; border-radius: 8px; padding: 40px; text-align: center;">
						<h1 style="font-size: 24px; font-weight: 600; margin: 0 0 8px; color: #2c2825;">Just Cook</h1>
						<p style="color: #78716c; margin: 0 0 32px; font-size: 14px;">Sign in to your account</p>

						<a href="${url}" style="display: inline-block; background: #2c2825; color: #ffffff; text-decoration: none; padding: 12px 32px; border-radius: 4px; font-weight: 500; font-size: 14px;">Sign in</a>

						<p style="color: #78716c; font-size: 13px; margin: 32px 0 0; line-height: 1.5;">
							This link expires in 10 minutes.<br>
							If you didn't request this, ignore this email.
						</p>
					</div>
				</body>
			</html>
		`
	});
}

export async function sendPasswordResetEmail(email: string, url: string) {
	await resend.emails.send({
		from: fromEmail,
		to: email,
		subject: 'Reset your password - Just Cook',
		html: `
			<!DOCTYPE html>
			<html>
				<head>
					<meta charset="utf-8">
					<meta name="viewport" content="width=device-width, initial-scale=1">
				</head>
				<body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f8f7f4; padding: 40px 20px; margin: 0;">
					<div style="max-width: 400px; margin: 0 auto; background: #ffffff; border-radius: 8px; padding: 40px; text-align: center;">
						<h1 style="font-size: 24px; font-weight: 600; margin: 0 0 8px; color: #2c2825;">Just Cook</h1>
						<p style="color: #78716c; margin: 0 0 32px; font-size: 14px;">Reset your password</p>

						<a href="${url}" style="display: inline-block; background: #2c2825; color: #ffffff; text-decoration: none; padding: 12px 32px; border-radius: 4px; font-weight: 500; font-size: 14px;">Reset password</a>

						<p style="color: #78716c; font-size: 13px; margin: 32px 0 0; line-height: 1.5;">
							This link expires in 1 hour.<br>
							If you didn't request this, ignore this email.
						</p>
					</div>
				</body>
			</html>
		`
	});
}

export async function sendVerificationEmail(email: string, url: string) {
	try {
		const result = await resend.emails.send({
			from: fromEmail,
			to: email,
			subject: 'Verify your email - Just Cook',
			html: `
				<!DOCTYPE html>
				<html>
					<head>
						<meta charset="utf-8">
						<meta name="viewport" content="width=device-width, initial-scale=1">
					</head>
					<body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f8f7f4; padding: 40px 20px; margin: 0;">
						<div style="max-width: 400px; margin: 0 auto; background: #ffffff; border-radius: 8px; padding: 40px; text-align: center;">
							<h1 style="font-size: 24px; font-weight: 600; margin: 0 0 8px; color: #2c2825;">Just Cook</h1>
							<p style="color: #78716c; margin: 0 0 32px; font-size: 14px;">Verify your email address</p>

							<a href="${url}" style="display: inline-block; background: #2c2825; color: #ffffff; text-decoration: none; padding: 12px 32px; border-radius: 4px; font-weight: 500; font-size: 14px;">Verify email</a>

							<p style="color: #78716c; font-size: 13px; margin: 32px 0 0; line-height: 1.5;">
								This link expires in 24 hours.<br>
								If you didn't create an account, ignore this email.
							</p>
						</div>
					</body>
				</html>
			`
		});
		return result;
	} catch (error) {
		logger.error('Failed to send verification email', error, { email });
		throw error;
	}
}
