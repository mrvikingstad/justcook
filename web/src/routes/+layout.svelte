<script lang="ts">
	import '../app.css';
	import Header from '$lib/components/layout/Header.svelte';
	import Footer from '$lib/components/layout/Footer.svelte';
	import { initBookmarks } from '$lib/stores/bookmarks';
	import { initUserLanguage } from '$lib/stores/languageMode';
	import { onMount } from 'svelte';

	let { data, children } = $props();

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

<div class="app-wrapper">
	<Header user={data.user} />

	<main>
		{@render children()}
	</main>

	<Footer />
</div>

<style>
	.app-wrapper {
		display: flex;
		flex-direction: column;
		min-height: 100vh;
	}

	.app-wrapper :global(> header) {
		flex-shrink: 0;
	}

	main {
		flex: 1 0 auto;
	}

	.app-wrapper :global(> footer) {
		flex-shrink: 0;
	}
</style>
