# sv

Everything you need to build a Svelte project, powered by [`sv`](https://github.com/sveltejs/cli).

## Creating a project

If you're seeing this, you've probably already done this step. Congrats!

```sh
# create a new project in the current directory
npx sv create

# create a new project in my-app
npx sv create my-app
```

## Developing

Once you've created a project and installed dependencies with `npm install` (or `pnpm install` or `yarn`), start a development server:

```sh
npm run dev

# or start the server and open the app in a new browser tab
npm run dev -- --open
```

## Building

To create a production version of your app:

```sh
npm run build
```

You can preview the production build with `npm run preview`.

> To deploy your app, you may need to install an [adapter](https://svelte.dev/docs/kit/adapters) for your target environment.


Hey Claude. This is JustCook, a super sophisticated, beautiful and minimalist way to find recipes online. I was wondering how close I am to an production ready release here? Can you scour the entire web app, highlight good/excellent architectural decisions, coding practices, etc., but more critically, find design flaws, potential scaling issues, loopholes, bottlenecks, etc.? "C:\Users\Eirik\Documents\Monet\justcook\web\src". 


Required Environment Variables for Production

DATABASE_URL           # PostgreSQL connection string
BETTER_AUTH_SECRET     # Auth session secret (32+ characters)
PUBLIC_APP_URL         # Your app URL (e.g., https://justcook.app)
GOOGLE_CLIENT_ID       # Google OAuth
GOOGLE_CLIENT_SECRET   # Google OAuth
RESEND_API_KEY         # Email service
OPENAI_API_KEY         # Content moderation (or set MODERATION_FALLBACK=queue)

Optional but Recommended

SENTRY_DSN             # Error tracking
UPSTASH_REDIS_REST_URL # Distributed rate limiting/caching
UPSTASH_REDIS_REST_TOKEN
R2_*                   # Image storage
ANTHROPIC_API_KEY      # AI cooking assistant