<script lang="ts">
	import '../app.css';
	import Header from '$lib/components/layout/Header.svelte';
	import Footer from '$lib/components/layout/Footer.svelte';
	import { initBookmarks } from '$lib/stores/bookmarks';
	import { initUserLanguage } from '$lib/stores/languageMode';
	import { page } from '$app/stores';
	import { onMount } from 'svelte';

	let { data, children } = $props();

	// AI page has its own layout without footer
	const isAiPage = $derived($page.url.pathname.startsWith('/ai'));

	// Initialize bookmarks store based on auth status
	onMount(() => {
		initBookmarks(!!data.user);
	});

	// Re-sync when auth status changes
	$effect(() => {
		initBookmarks(!!data.user);
	});

	// Initialize user language from server data
	$effect(() => {
		initUserLanguage(data.userLanguageCode);
	});
</script>

<svelte:head>
	<title>Just Cook</title>
	<meta name="description" content="Just recipes. Simple, as they should be." />
</svelte:head>

<div class="app-wrapper" class:ai-page={isAiPage}>
	<Header user={data.user} />

	<main>
		{@render children()}
	</main>

	{#if !isAiPage}
		<Footer />
	{/if}
</div>

<style>
	.app-wrapper {
		display: flex;
		flex-direction: column;
		min-height: 100vh;
	}

	.app-wrapper.ai-page {
		height: 100vh;
		overflow: hidden;
	}

	.app-wrapper :global(> header) {
		flex-shrink: 0;
	}

	main {
		flex: 1 0 auto;
	}

	.app-wrapper.ai-page main {
		overflow: hidden;
		display: flex;
		flex-direction: column;
	}

	.app-wrapper :global(> footer) {
		flex-shrink: 0;
	}
</style>
