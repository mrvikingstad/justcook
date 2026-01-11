<script lang="ts">
	import { Settings, Image, Trash2, Award, BookOpen, ArrowUp, ChevronDown, Eye, EyeOff, AtSign, Lock, AlertTriangle } from 'lucide-svelte';
	import { goto, invalidateAll } from '$app/navigation';
	import TierBadge from '$lib/components/ui/TierBadge.svelte';
	import ImageCropper from '$lib/components/ui/ImageCropper.svelte';
	import { countries } from '$lib/data/countries';
	import { portal } from '$lib/actions/portal';

	let { data } = $props();

	// Account section state
	let showEmail = $state(false);
	let newUsername = $state(data.account.displayUsername ?? '');
	let isChangingUsername = $state(false);
	let usernameError = $state('');
	let usernameSuccess = $state('');

	// Password change state
	let showPasswordForm = $state(false);
	let currentPassword = $state('');
	let newPassword = $state('');
	let confirmPassword = $state('');
	let isChangingPassword = $state(false);
	let passwordError = $state('');
	let passwordSuccess = $state('');

	// Delete account state
	let showDeleteConfirm = $state(false);
	let deleteConfirmation = $state('');
	let isDeleting = $state(false);
	let deleteError = $state('');

	// Form state - initialize from existing profile
	let fullName = $state(data.profile?.fullName ?? data.user.name ?? '');
	let country = $state(data.profile?.country ?? '');
	let bio = $state(data.profile?.bio ?? '');
	let photoFile = $state<File | null>(null);
	let photoBlob = $state<Blob | null>(null);
	let photoPreview = $state<string | null>(data.profile?.photoUrl ?? null);

	// Image cropper state
	let showCropper = $state(false);
	let cropperFile = $state<File | null>(null);

	// Country dropdown state
	let countryDropdownOpen = $state(false);
	let countrySearch = $state('');
	let countryTriggerEl: HTMLButtonElement;
	let countrySearchEl: HTMLInputElement;
	let countryDropdownX = $state(0);
	let countryDropdownY = $state(0);
	let countryDropdownWidth = $state(0);

	const BIO_MAX_LENGTH = 350;

	let isSaving = $state(false);
	let isUpgrading = $state(false);
	let error = $state('');
	let success = $state('');

	// Filtered countries for dropdown
	const filteredCountries = $derived(() => {
		const q = countrySearch.toLowerCase().trim();
		if (!q) return countries;
		return countries.filter(
			(c) =>
				c.name.toLowerCase().includes(q) ||
				c.code.toLowerCase() === q ||
				c.code3.toLowerCase() === q
		);
	});

	// Selected country display name
	const selectedCountryName = $derived(
		countries.find((c) => c.code === country)?.name ?? ''
	);

	function toggleCountryDropdown() {
		if (!countryDropdownOpen && countryTriggerEl) {
			const rect = countryTriggerEl.getBoundingClientRect();
			countryDropdownX = rect.left;
			countryDropdownY = rect.bottom + 4;
			countryDropdownWidth = Math.max(rect.width, 280);
		}
		countryDropdownOpen = !countryDropdownOpen;
		if (countryDropdownOpen) {
			countrySearch = '';
			// Focus the search input after the dropdown renders
			requestAnimationFrame(() => {
				countrySearchEl?.focus();
			});
		}
	}

	function handleScroll() {
		if (countryDropdownOpen && countryTriggerEl) {
			const rect = countryTriggerEl.getBoundingClientRect();
			countryDropdownX = rect.left;
			countryDropdownY = rect.bottom + 4;
		}
	}

	function selectCountry(code: string) {
		country = code;
		countryDropdownOpen = false;
	}

	function clearCountry() {
		country = '';
		countryDropdownOpen = false;
	}

	function handleCountryClickOutside(e: MouseEvent) {
		if (countryDropdownOpen && countryTriggerEl && !countryTriggerEl.contains(e.target as Node)) {
			const dropdown = document.querySelector('.country-dropdown');
			if (dropdown && !dropdown.contains(e.target as Node)) {
				countryDropdownOpen = false;
			}
		}
	}

	function handlePhotoChange(e: Event) {
		const input = e.target as HTMLInputElement;
		const file = input.files?.[0];
		if (file) {
			// Open cropper instead of directly setting preview
			cropperFile = file;
			showCropper = true;
		}
		// Reset input so same file can be selected again
		input.value = '';
	}

	function handleCropSave(blob: Blob, dataUrl: string) {
		photoBlob = blob;
		photoPreview = dataUrl;
		showCropper = false;
		cropperFile = null;
	}

	function handleCropCancel() {
		showCropper = false;
		cropperFile = null;
	}

	function removePhoto() {
		photoFile = null;
		photoBlob = null;
		photoPreview = null;
	}

	async function handleSave(e: SubmitEvent) {
		e.preventDefault();
		if (!fullName.trim()) {
			error = 'Full name is required';
			return;
		}

		isSaving = true;
		error = '';
		success = '';

		try {
			// Upload new photo if one was cropped
			let photoUrl: string | null = photoPreview;

			if (photoBlob) {
				const formData = new FormData();
				// Convert blob to file with a name
				const file = new File([photoBlob], 'profile.jpg', { type: photoBlob.type || 'image/jpeg' });
				formData.append('file', file);
				formData.append('folder', 'profiles');

				const uploadResponse = await fetch('/api/upload', {
					method: 'POST',
					body: formData
				});

				const uploadResult = await uploadResponse.json();

				if (!uploadResponse.ok) {
					error = uploadResult.error || 'Failed to upload photo';
					isSaving = false;
					return;
				}

				photoUrl = uploadResult.url;
			}

			const response = await fetch('/api/profile', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					fullName: fullName.trim(),
					country: country.trim(),
					bio: bio.trim(),
					photoUrl
				})
			});

			const result = await response.json();

			if (!response.ok) {
				error = result.error || 'Failed to save profile';
			} else {
				success = 'Profile saved successfully!';
				// Clear the blob since it's been uploaded
				photoBlob = null;
				await invalidateAll();
			}
		} catch (err) {
			error = 'Something went wrong. Please try again.';
		} finally {
			isSaving = false;
		}
	}

	async function handleUpgrade(targetTier: 'author' | 'chef') {
		isUpgrading = true;
		error = '';
		success = '';

		try {
			const response = await fetch('/api/profile/upgrade', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ targetTier })
			});

			const result = await response.json();

			if (!response.ok) {
				error = result.error || 'Failed to upgrade tier';
			} else {
				success = `Congratulations! You are now ${targetTier === 'author' ? 'an Author' : 'a Chef'}!`;
				await invalidateAll();
			}
		} catch (err) {
			error = 'Something went wrong. Please try again.';
		} finally {
			isUpgrading = false;
		}
	}

	// Username change handler
	async function handleUsernameChange() {
		if (data.account.usernameChangeCooldown > 0) {
			usernameError = `You can change your username in ${data.account.usernameChangeCooldown} days`;
			return;
		}

		if (!newUsername.trim()) {
			usernameError = 'Username is required';
			return;
		}

		isChangingUsername = true;
		usernameError = '';
		usernameSuccess = '';

		try {
			const response = await fetch('/api/account/username', {
				method: 'PUT',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ username: newUsername.trim() })
			});

			const result = await response.json();

			if (!response.ok) {
				usernameError = result.message || 'Failed to change username';
			} else {
				usernameSuccess = 'Username updated successfully!';
				await invalidateAll();
			}
		} catch (err) {
			usernameError = 'Something went wrong. Please try again.';
		} finally {
			isChangingUsername = false;
		}
	}

	// Password change handler
	async function handlePasswordChange() {
		if (!currentPassword) {
			passwordError = 'Current password is required';
			return;
		}

		if (!newPassword) {
			passwordError = 'New password is required';
			return;
		}

		if (newPassword.length < 8) {
			passwordError = 'New password must be at least 8 characters';
			return;
		}

		if (newPassword !== confirmPassword) {
			passwordError = 'Passwords do not match';
			return;
		}

		isChangingPassword = true;
		passwordError = '';
		passwordSuccess = '';

		try {
			const response = await fetch('/api/account/password', {
				method: 'PUT',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ currentPassword, newPassword })
			});

			const result = await response.json();

			if (!response.ok) {
				passwordError = result.message || 'Failed to change password';
			} else {
				passwordSuccess = 'Password changed successfully!';
				currentPassword = '';
				newPassword = '';
				confirmPassword = '';
				showPasswordForm = false;
			}
		} catch (err) {
			passwordError = 'Something went wrong. Please try again.';
		} finally {
			isChangingPassword = false;
		}
	}

	// Account deletion handler
	async function handleDeleteAccount() {
		if (deleteConfirmation !== 'DELETE') {
			deleteError = 'Please type DELETE to confirm';
			return;
		}

		isDeleting = true;
		deleteError = '';

		try {
			const response = await fetch('/api/account', {
				method: 'DELETE',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ confirmation: deleteConfirmation })
			});

			const result = await response.json();

			if (!response.ok) {
				deleteError = result.message || 'Failed to delete account';
			} else {
				goto('/');
			}
		} catch (err) {
			deleteError = 'Something went wrong. Please try again.';
		} finally {
			isDeleting = false;
		}
	}

	// Masked email display
	const maskedEmail = $derived(() => {
		const email = data.account.email;
		if (!email) return '';
		const [local, domain] = email.split('@');
		if (!local || !domain) return email;
		const maskedLocal = local.length <= 2
			? '*'.repeat(local.length)
			: local[0] + '*'.repeat(local.length - 2) + local[local.length - 1];
		return `${maskedLocal}@${domain}`;
	});

	// Progress calculations
	const nextTier = $derived(
		data.currentTier === 'user' ? 'author' : data.currentTier === 'author' ? 'chef' : null
	);

	const nextTierRequirements = $derived(
		nextTier ? data.tierRequirements[nextTier] : null
	);

	const recipeProgress = $derived(
		nextTierRequirements
			? Math.min(100, (data.stats.recipeCount / nextTierRequirements.recipes) * 100)
			: 100
	);

	const upvoteProgress = $derived(
		nextTierRequirements
			? Math.min(100, (data.stats.totalUpvotes / nextTierRequirements.upvotes) * 100)
			: 100
	);
</script>

<svelte:head>
	<title>Settings - Just Cook</title>
</svelte:head>

<svelte:window onclick={handleCountryClickOutside} onscroll={handleScroll} />

{#if showCropper && cropperFile}
	<ImageCropper
		file={cropperFile}
		onSave={handleCropSave}
		onCancel={handleCropCancel}
		outputSize={256}
	/>
{/if}

<div class="page container">
	<header class="page-header">
		<Settings size={24} />
		<h1>Settings</h1>
	</header>

	{#if error}
		<p class="message error">{error}</p>
	{/if}

	{#if success}
		<p class="message success">{success}</p>
	{/if}

	<!-- Profile Section -->
	<section class="section">
			<h2>Profile</h2>

			<form onsubmit={handleSave}>
				<!-- Photo -->
				<div class="photo-section">
					{#if photoPreview}
						<div class="photo-preview">
							<img src={photoPreview} alt="Profile" />
							<button type="button" class="remove-photo" onclick={removePhoto}>
								<Trash2 size={14} />
							</button>
						</div>
					{:else}
						<label class="photo-upload">
							<input type="file" accept="image/*" onchange={handlePhotoChange} />
							<Image size={24} />
						</label>
					{/if}
					<div class="photo-info">
						<p class="photo-label">Profile photo</p>
						<p class="photo-hint">Upload and crop to 256x256</p>
					</div>
				</div>

				<div class="field">
					<label for="fullName">Full name</label>
					<input
						type="text"
						id="fullName"
						bind:value={fullName}
						placeholder="Your full name"
						required
						maxlength="100"
						disabled={isSaving}
					/>
				</div>

				<div class="field">
					<label for="country">Country</label>
					<div class="country-select-wrapper">
						<button
							type="button"
							class="country-trigger"
							onclick={toggleCountryDropdown}
							bind:this={countryTriggerEl}
							disabled={isSaving}
							aria-haspopup="listbox"
							aria-expanded={countryDropdownOpen}
						>
							<span class="country-value" class:placeholder={!country}>
								{selectedCountryName || 'Select your country'}
							</span>
							<ChevronDown size={16} class="country-chevron {countryDropdownOpen ? 'open' : ''}" />
						</button>
					</div>
				</div>

				{#if countryDropdownOpen}
					<div
						class="country-dropdown"
						style="left: {countryDropdownX}px; top: {countryDropdownY}px; width: {countryDropdownWidth}px;"
						role="listbox"
						use:portal={'body'}
					>
						<div class="country-search-wrapper">
							<input
								type="text"
								class="country-search"
								placeholder="Search countries..."
								bind:value={countrySearch}
								bind:this={countrySearchEl}
							/>
						</div>
						<div class="country-options">
							{#if country}
								<button
									type="button"
									class="country-option clear-option"
									onclick={clearCountry}
								>
									Clear selection
								</button>
							{/if}
							{#each filteredCountries() as c}
								<button
									type="button"
									class="country-option"
									class:selected={c.code === country}
									onclick={() => selectCountry(c.code)}
									role="option"
									aria-selected={c.code === country}
								>
									<span class="country-name">{c.name}</span>
									<span class="country-code">{c.code}</span>
								</button>
							{:else}
								<div class="country-empty">No countries found</div>
							{/each}
						</div>
					</div>
				{/if}

				<div class="field">
					<label for="bio">Bio</label>
					<textarea
						id="bio"
						bind:value={bio}
						placeholder="Tell us about yourself and your cooking style..."
						rows="4"
						maxlength={BIO_MAX_LENGTH}
						disabled={isSaving}
					></textarea>
					<div class="bio-counter" class:warning={bio.length > BIO_MAX_LENGTH - 50} class:error={bio.length >= BIO_MAX_LENGTH}>
						{bio.length}/{BIO_MAX_LENGTH}
					</div>
				</div>

				<button type="submit" class="save-btn" disabled={isSaving || !fullName.trim()}>
					{isSaving ? 'Saving...' : 'Save profile'}
				</button>
			</form>
		</section>

		<!-- Account Section -->
		<section class="section">
			<h2>Account</h2>

			<!-- Username -->
			<div class="account-item">
				<div class="account-label">
					<AtSign size={16} />
					<span>Username</span>
				</div>
				<div class="account-content">
					<div class="username-form">
						<input
							type="text"
							bind:value={newUsername}
							placeholder="Username"
							disabled={isChangingUsername || data.account.usernameChangeCooldown > 0}
							maxlength="30"
						/>
						<button
							type="button"
							class="action-btn"
							onclick={handleUsernameChange}
							disabled={isChangingUsername || data.account.usernameChangeCooldown > 0 || newUsername === data.account.displayUsername}
						>
							{isChangingUsername ? 'Saving...' : 'Change'}
						</button>
					</div>
					{#if data.account.usernameChangeCooldown > 0}
						<p class="account-hint warning">
							You can change your username in {data.account.usernameChangeCooldown} day{data.account.usernameChangeCooldown === 1 ? '' : 's'}
						</p>
					{:else}
						<p class="account-hint">You can change your username once every 30 days</p>
					{/if}
					{#if usernameError}
						<p class="account-error">{usernameError}</p>
					{/if}
					{#if usernameSuccess}
						<p class="account-success">{usernameSuccess}</p>
					{/if}
				</div>
			</div>

			<!-- Email -->
			<div class="account-item">
				<div class="account-label">
					<Lock size={16} />
					<span>Email</span>
				</div>
				<div class="account-content">
					<div class="email-display">
						<span class="email-value">{showEmail ? data.account.email : maskedEmail()}</span>
						<button
							type="button"
							class="toggle-btn"
							onclick={() => showEmail = !showEmail}
							aria-label={showEmail ? 'Hide email' : 'Show email'}
						>
							{#if showEmail}
								<EyeOff size={16} />
							{:else}
								<Eye size={16} />
							{/if}
						</button>
					</div>
					<p class="account-hint">Your email is private and not shown publicly</p>
				</div>
			</div>

			<!-- Password -->
			{#if data.account.hasPassword}
				<div class="account-item">
					<div class="account-label">
						<Lock size={16} />
						<span>Password</span>
					</div>
					<div class="account-content">
						{#if !showPasswordForm}
							<button
								type="button"
								class="action-btn secondary"
								onclick={() => showPasswordForm = true}
							>
								Change password
							</button>
						{:else}
							<div class="password-form">
								<input
									type="password"
									bind:value={currentPassword}
									placeholder="Current password"
									disabled={isChangingPassword}
								/>
								<input
									type="password"
									bind:value={newPassword}
									placeholder="New password (min 8 characters)"
									disabled={isChangingPassword}
								/>
								<input
									type="password"
									bind:value={confirmPassword}
									placeholder="Confirm new password"
									disabled={isChangingPassword}
								/>
								<div class="password-actions">
									<button
										type="button"
										class="action-btn secondary"
										onclick={() => {
											showPasswordForm = false;
											currentPassword = '';
											newPassword = '';
											confirmPassword = '';
											passwordError = '';
										}}
										disabled={isChangingPassword}
									>
										Cancel
									</button>
									<button
										type="button"
										class="action-btn"
										onclick={handlePasswordChange}
										disabled={isChangingPassword}
									>
										{isChangingPassword ? 'Changing...' : 'Update password'}
									</button>
								</div>
							</div>
							{#if passwordError}
								<p class="account-error">{passwordError}</p>
							{/if}
						{/if}
						{#if passwordSuccess}
							<p class="account-success">{passwordSuccess}</p>
						{/if}
					</div>
				</div>
			{:else}
				<div class="account-item">
					<div class="account-label">
						<Lock size={16} />
						<span>Password</span>
					</div>
					<div class="account-content">
						<p class="account-hint">You signed up with a social provider. Password login is not available.</p>
					</div>
				</div>
			{/if}
		</section>

		<!-- Tier Section -->
		<section class="section tier-section">
			<h2>Profile Tier</h2>

			<div class="current-tier">
				<div class="tier-info">
					<span class="tier-label">Current tier:</span>
					<span class="tier-value">
						{#if data.currentTier === 'user'}
							Member
						{:else if data.currentTier === 'author'}
							<TierBadge tier="author" size="md" />
							Author
						{:else if data.currentTier === 'chef'}
							<TierBadge tier="chef" size="md" />
							Chef
						{/if}
					</span>
				</div>
			</div>

			{#if nextTier && nextTierRequirements}
				<div class="tier-progress">
					<h3>Progress to {nextTier === 'author' ? 'Author' : 'Chef'}</h3>

					<div class="progress-item">
						<div class="progress-header">
							<span class="progress-label">
								<BookOpen size={14} />
								Published recipes
							</span>
							<span class="progress-count">
								{data.stats.recipeCount} / {nextTierRequirements.recipes}
							</span>
						</div>
						<div class="progress-bar">
							<div class="progress-fill" style="width: {recipeProgress}%"></div>
						</div>
					</div>

					<div class="progress-item">
						<div class="progress-header">
							<span class="progress-label">
								<ArrowUp size={14} />
								Total upvotes
							</span>
							<span class="progress-count">
								{data.stats.totalUpvotes} / {nextTierRequirements.upvotes}
							</span>
						</div>
						<div class="progress-bar">
							<div class="progress-fill" style="width: {upvoteProgress}%"></div>
						</div>
					</div>

					{#if data.canClaimAuthor}
						<button
							class="upgrade-btn"
							onclick={() => handleUpgrade('author')}
							disabled={isUpgrading}
						>
							<Award size={18} />
							{isUpgrading ? 'Claiming...' : 'Claim Author status'}
						</button>
					{:else if data.canClaimChef}
						<button
							class="upgrade-btn chef"
							onclick={() => handleUpgrade('chef')}
							disabled={isUpgrading}
						>
							<img src="/chef.svg" alt="" class="upgrade-icon" />
							{isUpgrading ? 'Claiming...' : 'Claim Chef status'}
						</button>
					{:else}
						<p class="progress-hint">
							Keep cooking and sharing to unlock {nextTier === 'author' ? 'Author' : 'Chef'} status!
						</p>
					{/if}
				</div>
			{:else if data.currentTier === 'chef'}
				<p class="tier-max">You've reached the highest tier!</p>
			{/if}
	</section>

	<!-- Danger Zone -->
	<section class="section danger-zone">
		<h2>
			<AlertTriangle size={18} />
			Danger Zone
		</h2>

		<div class="danger-content">
			<div class="danger-info">
				<h3>Delete Account</h3>
				<p>Permanently delete your account and all associated data. This action cannot be undone.</p>
			</div>

			{#if !showDeleteConfirm}
				<button
					type="button"
					class="danger-btn"
					onclick={() => showDeleteConfirm = true}
				>
					Delete my account
				</button>
			{:else}
				<div class="delete-confirm">
					<p class="delete-warning">
						This will permanently delete:
					</p>
					<ul>
						<li>Your profile and account</li>
						<li>All your recipes</li>
						<li>All your comments and votes</li>
						<li>Your followers and following</li>
					</ul>
					<p class="delete-instruction">
						Type <strong>DELETE</strong> to confirm:
					</p>
					<input
						type="text"
						bind:value={deleteConfirmation}
						placeholder="Type DELETE"
						disabled={isDeleting}
					/>
					{#if deleteError}
						<p class="account-error">{deleteError}</p>
					{/if}
					<div class="delete-actions">
						<button
							type="button"
							class="action-btn secondary"
							onclick={() => {
								showDeleteConfirm = false;
								deleteConfirmation = '';
								deleteError = '';
							}}
							disabled={isDeleting}
						>
							Cancel
						</button>
						<button
							type="button"
							class="danger-btn"
							onclick={handleDeleteAccount}
							disabled={isDeleting || deleteConfirmation !== 'DELETE'}
						>
							{isDeleting ? 'Deleting...' : 'Permanently delete'}
						</button>
					</div>
				</div>
			{/if}
		</div>
	</section>
</div>

<style>
	.page {
		padding: 2rem 1.5rem 4rem;
		max-width: 640px;
	}

	.page-header {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		margin-bottom: 2rem;
	}

	.page-header h1 {
		font-size: 1.5rem;
		font-weight: 600;
		margin: 0;
	}

	.message {
		padding: 0.75rem 1rem;
		border-radius: 6px;
		font-size: 0.875rem;
		margin-bottom: 1.5rem;
	}

	.error {
		background: #fef2f2;
		color: #b91c1c;
	}

	:global(.dark) .error {
		background: #450a0a;
		color: #fca5a5;
	}

	.success {
		background: #f0fdf4;
		color: #166534;
	}

	:global(.dark) .success {
		background: #052e16;
		color: #86efac;
	}

	.notice {
		padding: 1rem;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 8px;
	}

	.notice p {
		margin: 0;
		color: var(--color-text-muted);
	}

	.section {
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 12px;
		padding: 1.5rem;
		margin-bottom: 1.5rem;
	}

	.section h2 {
		font-size: 1rem;
		font-weight: 600;
		margin: 0 0 1.25rem;
	}

	/* Photo section */
	.photo-section {
		display: flex;
		align-items: center;
		gap: 1rem;
		margin-bottom: 1.5rem;
	}

	.photo-preview {
		position: relative;
		width: 80px;
		height: 80px;
		flex-shrink: 0;
	}

	.photo-preview img {
		width: 100%;
		height: 100%;
		object-fit: cover;
		border-radius: 50%;
	}

	.remove-photo {
		position: absolute;
		bottom: -2px;
		right: -2px;
		width: 28px;
		height: 28px;
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		cursor: pointer;
		color: var(--color-text-muted);
		z-index: 1;
	}

	.remove-photo:hover {
		color: #b91c1c;
		border-color: #b91c1c;
	}

	.photo-upload {
		width: 80px;
		height: 80px;
		border-radius: 50%;
		border: 2px dashed var(--color-border);
		display: flex;
		align-items: center;
		justify-content: center;
		cursor: pointer;
		color: var(--color-text-muted);
		transition: border-color 0.15s, color 0.15s;
		flex-shrink: 0;
	}

	.photo-upload:hover {
		border-color: var(--color-text-muted);
		color: var(--color-text);
	}

	.photo-upload input {
		display: none;
	}

	.photo-info {
		flex: 1;
	}

	.photo-label {
		font-weight: 500;
		margin: 0 0 0.25rem;
	}

	.photo-hint {
		font-size: 0.8125rem;
		color: var(--color-text-muted);
		margin: 0;
	}

	/* Form fields */
	.field {
		margin-bottom: 1rem;
	}

	.field label {
		display: block;
		font-size: 0.875rem;
		font-weight: 500;
		margin-bottom: 0.375rem;
	}

	.field input,
	.field textarea {
		width: 100%;
		padding: 0.75rem;
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 6px;
		font-family: inherit;
		font-size: 0.9375rem;
		color: var(--color-text);
		resize: vertical;
	}

	.field input:focus,
	.field textarea:focus {
		outline: none;
		border-color: var(--color-text);
	}

	.field input::placeholder,
	.field textarea::placeholder {
		color: var(--color-text-muted);
	}

	.field input:disabled,
	.field textarea:disabled {
		opacity: 0.6;
	}

	.save-btn {
		padding: 0.75rem 1.5rem;
		background: var(--color-text);
		border: none;
		border-radius: 6px;
		color: var(--color-bg);
		font-family: inherit;
		font-size: 0.9375rem;
		font-weight: 500;
		cursor: pointer;
		transition: opacity 0.15s;
	}

	.save-btn:hover:not(:disabled) {
		opacity: 0.85;
	}

	.save-btn:disabled {
		opacity: 0.4;
		cursor: not-allowed;
	}

	/* Tier section */
	.tier-section h3 {
		font-size: 0.875rem;
		font-weight: 500;
		color: var(--color-text-muted);
		margin: 0 0 1rem;
	}

	.current-tier {
		display: flex;
		align-items: center;
		gap: 1rem;
		padding-bottom: 1.25rem;
		border-bottom: 1px solid var(--color-border);
		margin-bottom: 1.25rem;
	}

	.tier-info {
		display: flex;
		align-items: center;
		gap: 0.5rem;
	}

	.tier-label {
		color: var(--color-text-muted);
		font-size: 0.9375rem;
	}

	.tier-value {
		display: flex;
		align-items: center;
		gap: 0.375rem;
		font-weight: 600;
	}

	.tier-progress {
		margin-top: 0.5rem;
	}

	.progress-item {
		margin-bottom: 1rem;
	}

	.progress-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 0.5rem;
	}

	.progress-label {
		display: flex;
		align-items: center;
		gap: 0.375rem;
		font-size: 0.875rem;
		color: var(--color-text-muted);
	}

	.progress-count {
		font-size: 0.875rem;
		font-weight: 500;
	}

	.progress-bar {
		height: 8px;
		background: var(--color-border);
		border-radius: 4px;
		overflow: hidden;
	}

	.progress-fill {
		height: 100%;
		background: var(--color-text);
		border-radius: 4px;
		transition: width 0.3s ease;
	}

	.upgrade-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 0.5rem;
		width: 100%;
		padding: 0.875rem 1.5rem;
		background: var(--color-text);
		border: none;
		border-radius: 8px;
		color: var(--color-bg);
		font-family: inherit;
		font-size: 1rem;
		font-weight: 600;
		cursor: pointer;
		transition: opacity 0.15s;
		margin-top: 1rem;
	}

	.upgrade-btn:hover:not(:disabled) {
		opacity: 0.85;
	}

	.upgrade-btn:disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}

	.upgrade-icon {
		width: 20px;
		height: 20px;
	}

	:global(.dark) .upgrade-btn .upgrade-icon {
		filter: invert(1);
	}

	.progress-hint {
		font-size: 0.875rem;
		color: var(--color-text-muted);
		margin: 1rem 0 0;
		text-align: center;
	}

	.tier-max {
		font-size: 0.9375rem;
		color: var(--color-text-muted);
		margin: 0;
	}

	/* Country dropdown */
	.country-select-wrapper {
		position: relative;
	}

	.country-trigger {
		display: flex;
		align-items: center;
		justify-content: space-between;
		width: 100%;
		padding: 0.75rem;
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 6px;
		font-family: inherit;
		font-size: 0.9375rem;
		color: var(--color-text);
		cursor: pointer;
		transition: border-color 0.15s;
	}

	.country-trigger:hover:not(:disabled) {
		border-color: var(--color-text-muted);
	}

	.country-trigger:focus {
		outline: none;
		border-color: var(--color-text);
	}

	.country-trigger:disabled {
		opacity: 0.6;
		cursor: not-allowed;
	}

	.country-value {
		text-align: left;
	}

	.country-value.placeholder {
		color: var(--color-text-muted);
	}

	:global(.country-chevron) {
		color: var(--color-text-muted);
		transition: transform 0.2s;
		flex-shrink: 0;
	}

	:global(.country-chevron.open) {
		transform: rotate(180deg);
	}

	.country-dropdown {
		position: fixed;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 8px;
		box-shadow: var(--shadow-elevation-medium);
		z-index: 9999;
		overflow: hidden;
		max-height: 320px;
		display: flex;
		flex-direction: column;
	}

	.country-search-wrapper {
		padding: 0.5rem;
		border-bottom: 1px solid var(--color-border);
	}

	.country-search {
		width: 100%;
		padding: 0.5rem 0.75rem;
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 4px;
		font-family: inherit;
		font-size: 0.875rem;
		color: var(--color-text);
	}

	.country-search:focus {
		outline: none;
		border-color: var(--color-text-muted);
	}

	.country-search::placeholder {
		color: var(--color-text-muted);
	}

	.country-options {
		overflow-y: auto;
		flex: 1;
	}

	.country-option {
		display: flex;
		align-items: center;
		justify-content: space-between;
		width: 100%;
		padding: 0.625rem 0.75rem;
		background: none;
		border: none;
		font-family: inherit;
		font-size: 0.875rem;
		color: var(--color-text);
		text-align: left;
		cursor: pointer;
		transition: background-color 0.1s;
	}

	.country-option:hover {
		background: var(--color-border);
	}

	.country-option.selected {
		background: var(--color-border);
		font-weight: 500;
	}

	.country-option.clear-option {
		color: var(--color-text-muted);
		border-bottom: 1px solid var(--color-border);
	}

	.country-name {
		flex: 1;
	}

	.country-code {
		color: var(--color-text-muted);
		font-size: 0.75rem;
		margin-left: 0.5rem;
	}

	.country-empty {
		padding: 1rem;
		text-align: center;
		color: var(--color-text-muted);
		font-size: 0.875rem;
	}

	/* Bio counter */
	.bio-counter {
		margin-top: 0.375rem;
		font-size: 0.75rem;
		color: var(--color-text-muted);
		text-align: right;
	}

	.bio-counter.warning {
		color: #ca8a04;
	}

	:global(.dark) .bio-counter.warning {
		color: #facc15;
	}

	.bio-counter.error {
		color: #dc2626;
	}

	:global(.dark) .bio-counter.error {
		color: #f87171;
	}

	/* Account section */
	.account-item {
		padding: 1rem 0;
		border-bottom: 1px solid var(--color-border);
	}

	.account-item:first-of-type {
		padding-top: 0;
	}

	.account-item:last-of-type {
		border-bottom: none;
		padding-bottom: 0;
	}

	.account-label {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		font-size: 0.875rem;
		font-weight: 500;
		margin-bottom: 0.5rem;
		color: var(--color-text-muted);
	}

	.account-content {
		margin-left: 1.5rem;
	}

	.username-form {
		display: flex;
		gap: 0.5rem;
	}

	.username-form input {
		flex: 1;
		padding: 0.625rem 0.75rem;
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 6px;
		font-family: inherit;
		font-size: 0.9375rem;
		color: var(--color-text);
	}

	.username-form input:focus {
		outline: none;
		border-color: var(--color-text);
	}

	.username-form input:disabled {
		opacity: 0.6;
	}

	.action-btn {
		padding: 0.625rem 1rem;
		background: var(--color-text);
		border: none;
		border-radius: 6px;
		color: var(--color-bg);
		font-family: inherit;
		font-size: 0.875rem;
		font-weight: 500;
		cursor: pointer;
		transition: opacity 0.15s;
		white-space: nowrap;
	}

	.action-btn:hover:not(:disabled) {
		opacity: 0.85;
	}

	.action-btn:disabled {
		opacity: 0.4;
		cursor: not-allowed;
	}

	.action-btn.secondary {
		background: var(--color-surface);
		color: var(--color-text);
		border: 1px solid var(--color-border);
	}

	.action-btn.secondary:hover:not(:disabled) {
		border-color: var(--color-text);
	}

	.account-hint {
		font-size: 0.8125rem;
		color: var(--color-text-muted);
		margin: 0.5rem 0 0;
	}

	.account-hint.warning {
		color: #ca8a04;
	}

	:global(.dark) .account-hint.warning {
		color: #facc15;
	}

	.account-error {
		font-size: 0.8125rem;
		color: #dc2626;
		margin: 0.5rem 0 0;
	}

	:global(.dark) .account-error {
		color: #f87171;
	}

	.account-success {
		font-size: 0.8125rem;
		color: #16a34a;
		margin: 0.5rem 0 0;
	}

	:global(.dark) .account-success {
		color: #4ade80;
	}

	.email-display {
		display: flex;
		align-items: center;
		gap: 0.5rem;
	}

	.email-value {
		font-size: 0.9375rem;
		font-family: monospace;
	}

	.toggle-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 0.375rem;
		background: none;
		border: 1px solid var(--color-border);
		border-radius: 4px;
		color: var(--color-text-muted);
		cursor: pointer;
	}

	.toggle-btn:hover {
		border-color: var(--color-text);
		color: var(--color-text);
	}

	.password-form {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}

	.password-form input {
		padding: 0.625rem 0.75rem;
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 6px;
		font-family: inherit;
		font-size: 0.9375rem;
		color: var(--color-text);
	}

	.password-form input:focus {
		outline: none;
		border-color: var(--color-text);
	}

	.password-form input:disabled {
		opacity: 0.6;
	}

	.password-form input::placeholder {
		color: var(--color-text-muted);
	}

	.password-actions {
		display: flex;
		gap: 0.5rem;
		margin-top: 0.5rem;
	}

	/* Danger Zone */
	.danger-zone {
		border-color: #dc2626;
	}

	.danger-zone h2 {
		display: flex;
		align-items: center;
		gap: 0.5rem;
		color: #dc2626;
	}

	:global(.dark) .danger-zone {
		border-color: #f87171;
	}

	:global(.dark) .danger-zone h2 {
		color: #f87171;
	}

	.danger-content {
		display: flex;
		flex-direction: column;
		gap: 1rem;
	}

	.danger-info h3 {
		font-size: 0.9375rem;
		font-weight: 600;
		margin: 0 0 0.25rem;
	}

	.danger-info p {
		font-size: 0.875rem;
		color: var(--color-text-muted);
		margin: 0;
	}

	.danger-btn {
		padding: 0.625rem 1rem;
		background: #dc2626;
		border: none;
		border-radius: 6px;
		color: white;
		font-family: inherit;
		font-size: 0.875rem;
		font-weight: 500;
		cursor: pointer;
		transition: background-color 0.15s;
		align-self: flex-start;
	}

	.danger-btn:hover:not(:disabled) {
		background: #b91c1c;
	}

	.danger-btn:disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}

	.delete-confirm {
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 8px;
		padding: 1rem;
	}

	.delete-warning {
		font-size: 0.875rem;
		font-weight: 500;
		margin: 0 0 0.5rem;
	}

	.delete-confirm ul {
		margin: 0 0 1rem;
		padding-left: 1.25rem;
		font-size: 0.875rem;
		color: var(--color-text-muted);
	}

	.delete-confirm li {
		margin-bottom: 0.25rem;
	}

	.delete-instruction {
		font-size: 0.875rem;
		margin: 0 0 0.5rem;
	}

	.delete-confirm input {
		width: 100%;
		padding: 0.625rem 0.75rem;
		background: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: 6px;
		font-family: inherit;
		font-size: 0.9375rem;
		color: var(--color-text);
		margin-bottom: 0.5rem;
	}

	.delete-confirm input:focus {
		outline: none;
		border-color: #dc2626;
	}

	.delete-confirm input::placeholder {
		color: var(--color-text-muted);
	}

	.delete-actions {
		display: flex;
		gap: 0.5rem;
		margin-top: 0.5rem;
	}

	/* Mobile */
	@media (max-width: 640px) {
		.page {
			padding: 1.5rem 1rem 3rem;
		}

		.page-header h1 {
			font-size: 1.25rem;
		}

		.section {
			padding: 1.25rem;
		}

		.photo-preview,
		.photo-upload {
			width: 64px;
			height: 64px;
		}
	}
</style>
