<script lang="ts">
	import { X, ZoomIn, ZoomOut, RotateCcw, RefreshCw } from 'lucide-svelte';
	import { portal } from '$lib/actions/portal';

	interface Props {
		file: File;
		onSave: (blob: Blob, dataUrl: string) => void;
		onCancel: () => void;
		outputSize?: number;
	}

	let { file, onSave, onCancel, outputSize = 256 }: Props = $props();

	let previewCanvas: HTMLCanvasElement;
	let saveCanvas: HTMLCanvasElement;
	let loadedImage: HTMLImageElement | null = null;

	// State
	let ready = $state(false);
	let imageWidth = $state(0);
	let imageHeight = $state(0);
	
	// Position of image CENTER within the crop area
	let centerX = $state(150);
	let centerY = $state(150);
	let scale = $state(1);
	let rotation = $state(0);

	// Drag state
	let isDragging = false;
	let dragStartX = 0;
	let dragStartY = 0;
	let dragStartCenterX = 0;
	let dragStartCenterY = 0;

	const containerSize = 300;

	// Minimum scale to cover container
	function getMinScale(w: number, h: number, rot: number): number {
		const isRotated = rot === 90 || rot === 270;
		const effW = isRotated ? h : w;
		const effH = isRotated ? w : h;
		if (effW === 0 || effH === 0) return 1;
		return containerSize / Math.min(effW, effH);
	}

	// Scaled dimensions considering rotation
	function getScaledDims(w: number, h: number, rot: number, sc: number) {
		const isRotated = rot === 90 || rot === 270;
		return {
			w: (isRotated ? h : w) * sc,
			h: (isRotated ? w : h) * sc
		};
	}

	// Constrain center to prevent gaps
	function constrain(cx: number, cy: number, w: number, h: number, rot: number, sc: number) {
		const dims = getScaledDims(w, h, rot, sc);
		const halfW = dims.w / 2;
		const halfH = dims.h / 2;
		return {
			x: Math.max(containerSize - halfW, Math.min(halfW, cx)),
			y: Math.max(containerSize - halfH, Math.min(halfH, cy))
		};
	}

	// Render the preview canvas
	function renderPreview() {
		if (!previewCanvas || !loadedImage || !ready) return;
		
		const ctx = previewCanvas.getContext('2d');
		if (!ctx) return;

		// Clear
		ctx.clearRect(0, 0, containerSize, containerSize);

		// Draw image: translate to center, rotate, scale, draw centered
		ctx.save();
		ctx.translate(centerX, centerY);
		ctx.rotate((rotation * Math.PI) / 180);
		ctx.scale(scale, scale);
		ctx.drawImage(loadedImage, -imageWidth / 2, -imageHeight / 2);
		ctx.restore();
	}

	// Load image
	$effect(() => {
		const img = new Image();
		const objectUrl = URL.createObjectURL(file);
		
		img.onload = () => {
			const w = img.naturalWidth;
			const h = img.naturalHeight;
			const initScale = getMinScale(w, h, 0);
			
			loadedImage = img;
			imageWidth = w;
			imageHeight = h;
			scale = initScale;
			rotation = 0;
			centerX = containerSize / 2;
			centerY = containerSize / 2;
			ready = true;
		};
		
		img.src = objectUrl;
		
		return () => URL.revokeObjectURL(objectUrl);
	});

	// Re-render preview whenever state changes
	$effect(() => {
		if (ready && previewCanvas && loadedImage) {
			// Access reactive variables to create dependency
			void centerX;
			void centerY;
			void scale;
			void rotation;
			renderPreview();
		}
	});

	const zoomLevel = $derived(
		getMinScale(imageWidth, imageHeight, rotation) > 0 
			? scale / getMinScale(imageWidth, imageHeight, rotation) 
			: 1
	);

	function zoomTo(newScale: number, pivotX: number, pivotY: number) {
		if (scale === newScale) return;
		const ratio = newScale / scale;
		let newCX = pivotX + (centerX - pivotX) * ratio;
		let newCY = pivotY + (centerY - pivotY) * ratio;
		const c = constrain(newCX, newCY, imageWidth, imageHeight, rotation, newScale);
		scale = newScale;
		centerX = c.x;
		centerY = c.y;
	}

	function handleZoomIn() {
		const minScale = getMinScale(imageWidth, imageHeight, rotation);
		zoomTo(Math.min(scale * 1.2, minScale * 4), containerSize / 2, containerSize / 2);
	}

	function handleZoomOut() {
		const minScale = getMinScale(imageWidth, imageHeight, rotation);
		zoomTo(Math.max(scale / 1.2, minScale), containerSize / 2, containerSize / 2);
	}

	function handleSliderChange(e: Event) {
		const val = parseFloat((e.target as HTMLInputElement).value);
		const minScale = getMinScale(imageWidth, imageHeight, rotation);
		zoomTo(minScale * val, containerSize / 2, containerSize / 2);
	}

	function handleReset() {
		rotation = 0;
		scale = getMinScale(imageWidth, imageHeight, 0);
		centerX = containerSize / 2;
		centerY = containerSize / 2;
	}

	function handleRotate() {
		const newRot = (rotation + 90) % 360;
		const newMinScale = getMinScale(imageWidth, imageHeight, newRot);
		const newScale = Math.max(scale, newMinScale);
		rotation = newRot;
		scale = newScale;
		centerX = containerSize / 2;
		centerY = containerSize / 2;
	}

	function handleWheel(e: WheelEvent) {
		e.preventDefault();
		const minScale = getMinScale(imageWidth, imageHeight, rotation);
		const delta = e.deltaY > 0 ? 0.9 : 1.1;
		const newScale = Math.max(minScale, Math.min(minScale * 4, scale * delta));
		const rect = (e.currentTarget as HTMLElement).getBoundingClientRect();
		zoomTo(newScale, e.clientX - rect.left, e.clientY - rect.top);
	}

	function handleMouseDown(e: MouseEvent) {
		isDragging = true;
		dragStartX = e.clientX;
		dragStartY = e.clientY;
		dragStartCenterX = centerX;
		dragStartCenterY = centerY;
	}

	function handleMouseMove(e: MouseEvent) {
		if (!isDragging) return;
		const newCX = dragStartCenterX + (e.clientX - dragStartX);
		const newCY = dragStartCenterY + (e.clientY - dragStartY);
		const c = constrain(newCX, newCY, imageWidth, imageHeight, rotation, scale);
		centerX = c.x;
		centerY = c.y;
	}

	function handleMouseUp() {
		isDragging = false;
	}

	function handleTouchStart(e: TouchEvent) {
		if (e.touches.length === 1) {
			isDragging = true;
			dragStartX = e.touches[0].clientX;
			dragStartY = e.touches[0].clientY;
			dragStartCenterX = centerX;
			dragStartCenterY = centerY;
		}
	}

	function handleTouchMove(e: TouchEvent) {
		if (!isDragging || e.touches.length !== 1) return;
		e.preventDefault();
		const newCX = dragStartCenterX + (e.touches[0].clientX - dragStartX);
		const newCY = dragStartCenterY + (e.touches[0].clientY - dragStartY);
		const c = constrain(newCX, newCY, imageWidth, imageHeight, rotation, scale);
		centerX = c.x;
		centerY = c.y;
	}

	function handleTouchEnd() {
		isDragging = false;
	}

	async function handleSave() {
		if (!ready || !saveCanvas || !loadedImage) return;

		const ctx = saveCanvas.getContext('2d');
		if (!ctx) return;

		// Render at container size first
		const tempCanvas = document.createElement('canvas');
		tempCanvas.width = containerSize;
		tempCanvas.height = containerSize;
		const tempCtx = tempCanvas.getContext('2d');
		if (!tempCtx) return;

		// Same rendering as preview
		tempCtx.translate(centerX, centerY);
		tempCtx.rotate((rotation * Math.PI) / 180);
		tempCtx.scale(scale, scale);
		tempCtx.drawImage(loadedImage, -imageWidth / 2, -imageHeight / 2);

		// Scale to output size
		saveCanvas.width = outputSize;
		saveCanvas.height = outputSize;
		ctx.drawImage(tempCanvas, 0, 0, containerSize, containerSize, 0, 0, outputSize, outputSize);

		saveCanvas.toBlob(
			(blob) => {
				if (blob) {
					const dataUrl = saveCanvas.toDataURL('image/webp', 0.85);
					onSave(blob, dataUrl);
				}
			},
			'image/webp',
			0.85
		);
	}
</script>

<svelte:window onmouseup={handleMouseUp} onmousemove={handleMouseMove} />

<div class="overlay" use:portal={'body'}>
	<div class="modal">
		<div class="header">
			<h3>Crop photo</h3>
			<button class="close-btn" onclick={onCancel} aria-label="Close">
				<X size={20} />
			</button>
		</div>

		<div class="content">
			<div
				class="crop-area"
				style="width: {containerSize}px; height: {containerSize}px;"
				onmousedown={handleMouseDown}
				ontouchstart={handleTouchStart}
				ontouchmove={handleTouchMove}
				ontouchend={handleTouchEnd}
				onwheel={handleWheel}
				role="application"
				aria-label="Drag to reposition, scroll to zoom"
			>
				<canvas
					bind:this={previewCanvas}
					width={containerSize}
					height={containerSize}
					class="preview-canvas"
				></canvas>
				{#if !ready}
					<div class="loading">Loading...</div>
				{/if}
				<div class="crop-overlay">
					<div class="crop-circle"></div>
				</div>
			</div>

			<div class="controls">
				<button class="control-btn" onclick={handleZoomOut} aria-label="Zoom out">
					<ZoomOut size={18} />
				</button>
				<input
					type="range"
					min="1"
					max="4"
					step="0.01"
					value={zoomLevel}
					oninput={handleSliderChange}
					class="zoom-slider"
					aria-label="Zoom level"
				/>
				<button class="control-btn" onclick={handleZoomIn} aria-label="Zoom in">
					<ZoomIn size={18} />
				</button>
				<button class="control-btn" onclick={handleRotate} aria-label="Rotate 90°">
					<RotateCcw size={18} />
				</button>
				<button class="control-btn" onclick={handleReset} aria-label="Reset">
					<RefreshCw size={18} />
				</button>
			</div>

			<p class="hint">Drag to reposition, scroll to zoom</p>
		</div>

		<div class="footer">
			<button class="btn secondary" onclick={onCancel}>Cancel</button>
			<button class="btn primary" onclick={handleSave} disabled={!ready}>
				Save photo
			</button>
		</div>

		<canvas bind:this={saveCanvas} style="display: none;"></canvas>
	</div>
</div>

<style>
	.overlay {
		position: fixed;
		inset: 0;
		background: rgba(0, 0, 0, 0.6);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 10000;
		padding: 1rem;
	}

	.modal {
		background: var(--color-surface);
		border-radius: 12px;
		max-width: 400px;
		width: 100%;
		overflow: hidden;
		box-shadow: var(--shadow-elevation-high);
	}

	.header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 1rem 1.25rem;
		border-bottom: 1px solid var(--color-border);
	}

	.header h3 {
		margin: 0;
		font-size: 1rem;
		font-weight: 600;
	}

	.close-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 32px;
		height: 32px;
		background: none;
		border: none;
		border-radius: 6px;
		color: var(--color-text-muted);
		cursor: pointer;
		transition: background-color 0.15s, color 0.15s;
	}

	.close-btn:hover {
		background: var(--color-border);
		color: var(--color-text);
	}

	.content {
		padding: 1.5rem;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 1rem;
	}

	.crop-area {
		position: relative;
		background: var(--color-bg);
		border-radius: 8px;
		overflow: hidden;
		cursor: grab;
		touch-action: none;
		user-select: none;
	}

	.crop-area:active {
		cursor: grabbing;
	}

	.preview-canvas {
		display: block;
	}

	.loading {
		position: absolute;
		inset: 0;
		display: flex;
		align-items: center;
		justify-content: center;
		color: var(--color-text-muted);
		font-size: 0.875rem;
		background: var(--color-bg);
	}

	.crop-overlay {
		position: absolute;
		inset: 0;
		pointer-events: none;
	}

	.crop-circle {
		position: absolute;
		inset: 0;
		border-radius: 50%;
		box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.5);
	}

	.controls {
		display: flex;
		align-items: center;
		gap: 0.75rem;
	}

	.control-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 36px;
		height: 36px;
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 6px;
		color: var(--color-text);
		cursor: pointer;
		transition: background-color 0.15s, border-color 0.15s;
	}

	.control-btn:hover {
		background: var(--color-border);
	}

	.zoom-slider {
		width: 120px;
		height: 4px;
		-webkit-appearance: none;
		appearance: none;
		background: var(--color-border);
		border-radius: 2px;
		outline: none;
	}

	.zoom-slider::-webkit-slider-thumb {
		-webkit-appearance: none;
		appearance: none;
		width: 16px;
		height: 16px;
		background: var(--color-text);
		border-radius: 50%;
		cursor: pointer;
	}

	.zoom-slider::-moz-range-thumb {
		width: 16px;
		height: 16px;
		background: var(--color-text);
		border-radius: 50%;
		cursor: pointer;
		border: none;
	}

	.hint {
		font-size: 0.75rem;
		color: var(--color-text-muted);
		margin: 0;
	}

	.footer {
		display: flex;
		justify-content: flex-end;
		gap: 0.75rem;
		padding: 1rem 1.25rem;
		border-top: 1px solid var(--color-border);
	}

	.btn {
		padding: 0.625rem 1.25rem;
		border: none;
		border-radius: 6px;
		font-family: inherit;
		font-size: 0.875rem;
		font-weight: 500;
		cursor: pointer;
		transition: opacity 0.15s, background-color 0.15s;
	}

	.btn.secondary {
		background: var(--color-bg);
		border: 1px solid var(--color-border);
		color: var(--color-text);
	}

	.btn.secondary:hover {
		background: var(--color-border);
	}

	.btn.primary {
		background: var(--color-text);
		color: var(--color-bg);
	}

	.btn.primary:hover:not(:disabled) {
		opacity: 0.85;
	}

	.btn:disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}

	@media (max-width: 440px) {
		.modal {
			max-width: calc(100vw - 2rem);
		}

		.content {
			padding: 1rem;
		}
	}
</style>