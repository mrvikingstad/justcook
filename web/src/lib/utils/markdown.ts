import { marked } from 'marked';
import DOMPurify from 'isomorphic-dompurify';

// Configure marked for cooking-focused rendering
marked.setOptions({
	breaks: true, // Convert \n to <br>
	gfm: true // GitHub Flavored Markdown
});

// Custom renderer for styling consistency
const renderer = new marked.Renderer();

// Style code blocks with cooking theme
renderer.code = function ({ text, lang }) {
	return `<pre class="code-block"><code class="language-${lang || 'text'}">${escapeHtml(text)}</code></pre>`;
};

// Style inline code
renderer.codespan = function ({ text }) {
	return `<code class="inline-code">${escapeHtml(text)}</code>`;
};

// Style headings
renderer.heading = function ({ tokens, depth }) {
	const text = this.parser.parseInline(tokens);
	const tag = `h${depth}`;
	return `<${tag} class="md-heading md-h${depth}">${text}</${tag}>`;
};

// Style blockquotes (tips from the chef)
renderer.blockquote = function ({ tokens }) {
	const body = this.parser.parse(tokens);
	return `<blockquote class="chef-tip">${body}</blockquote>`;
};

// Style links
renderer.link = function ({ href, title, tokens }) {
	const text = this.parser.parseInline(tokens);
	const safeHref = href && href.startsWith('http') ? href : '#';
	return `<a href="${safeHref}" target="_blank" rel="noopener noreferrer" class="md-link"${title ? ` title="${title}"` : ''}>${text}</a>`;
};

marked.use({ renderer });

function escapeHtml(text: string): string {
	const map: Record<string, string> = {
		'&': '&amp;',
		'<': '&lt;',
		'>': '&gt;',
		'"': '&quot;',
		"'": '&#039;'
	};
	return text.replace(/[&<>"']/g, (m) => map[m]);
}

export function renderMarkdown(content: string): string {
	// Parse markdown
	const html = marked.parse(content) as string;

	// Sanitize HTML to prevent XSS
	const clean = DOMPurify.sanitize(html, {
		ALLOWED_TAGS: [
			'p',
			'br',
			'strong',
			'em',
			'b',
			'i',
			'u',
			'h1',
			'h2',
			'h3',
			'h4',
			'h5',
			'h6',
			'ul',
			'ol',
			'li',
			'blockquote',
			'pre',
			'code',
			'a',
			'hr',
			'table',
			'thead',
			'tbody',
			'tr',
			'th',
			'td'
		],
		ALLOWED_ATTR: ['href', 'target', 'rel', 'title', 'class']
	});

	return clean;
}
