// Language repository mapping ISO 639-1 codes to language names
// Used for recipe language selection

export interface LanguageDef {
	/** ISO 639-1 code (2-letter) */
	code: string;
	/** Language name in English */
	name: string;
	/** Language name in the native script */
	nativeName: string;
}

export const languages: LanguageDef[] = [
	{ code: 'am', name: 'Amharic', nativeName: 'አማርኛ' },
	{ code: 'ar', name: 'Arabic', nativeName: 'العربية' },
	{ code: 'az', name: 'Azerbaijani', nativeName: 'Azərbaycan' },
	{ code: 'be', name: 'Belarusian', nativeName: 'Беларуская' },
	{ code: 'bg', name: 'Bulgarian', nativeName: 'Български' },
	{ code: 'bi', name: 'Bislama', nativeName: 'Bislama' },
	{ code: 'bn', name: 'Bengali', nativeName: 'বাংলা' },
	{ code: 'bs', name: 'Bosnian', nativeName: 'Bosanski' },
	{ code: 'ca', name: 'Catalan', nativeName: 'Català' },
	{ code: 'cs', name: 'Czech', nativeName: 'Čeština' },
	{ code: 'da', name: 'Danish', nativeName: 'Dansk' },
	{ code: 'de', name: 'German', nativeName: 'Deutsch' },
	{ code: 'dv', name: 'Dhivehi', nativeName: 'ދިވެހި' },
	{ code: 'dz', name: 'Dzongkha', nativeName: 'རྫོང་ཁ' },
	{ code: 'el', name: 'Greek', nativeName: 'Ελληνικά' },
	{ code: 'en', name: 'English', nativeName: 'English' },
	{ code: 'es', name: 'Spanish', nativeName: 'Español' },
	{ code: 'et', name: 'Estonian', nativeName: 'Eesti' },
	{ code: 'fa', name: 'Persian', nativeName: 'فارسی' },
	{ code: 'fi', name: 'Finnish', nativeName: 'Suomi' },
	{ code: 'fr', name: 'French', nativeName: 'Français' },
	{ code: 'he', name: 'Hebrew', nativeName: 'עברית' },
	{ code: 'hi', name: 'Hindi', nativeName: 'हिन्दी' },
	{ code: 'hr', name: 'Croatian', nativeName: 'Hrvatski' },
	{ code: 'hu', name: 'Hungarian', nativeName: 'Magyar' },
	{ code: 'hy', name: 'Armenian', nativeName: 'Hayeren' },
	{ code: 'id', name: 'Indonesian', nativeName: 'Bahasa Indonesia' },
	{ code: 'is', name: 'Icelandic', nativeName: 'Íslenska' },
	{ code: 'it', name: 'Italian', nativeName: 'Italiano' },
	{ code: 'ja', name: 'Japanese', nativeName: '日本語' },
	{ code: 'ka', name: 'Georgian', nativeName: 'ქართული' },
	{ code: 'kk', name: 'Kazakh', nativeName: 'Қазақша' },
	{ code: 'km', name: 'Khmer', nativeName: 'ភាសាខ្មែរ' },
	{ code: 'ko', name: 'Korean', nativeName: '한국어' },
	{ code: 'ky', name: 'Kyrgyz', nativeName: 'Кыргызча' },
	{ code: 'lb', name: 'Luxembourgish', nativeName: 'Lëtzebuergesch' },
	{ code: 'lo', name: 'Lao', nativeName: 'ລາວ' },
	{ code: 'lt', name: 'Lithuanian', nativeName: 'Lietuvių' },
	{ code: 'lv', name: 'Latvian', nativeName: 'Latviešu' },
	{ code: 'mg', name: 'Malagasy', nativeName: 'Malagasy' },
	{ code: 'mk', name: 'Macedonian', nativeName: 'Македонски' },
	{ code: 'mn', name: 'Mongolian', nativeName: 'Монгол' },
	{ code: 'ms', name: 'Malay', nativeName: 'Bahasa Melayu' },
	{ code: 'mt', name: 'Maltese', nativeName: 'Malti' },
	{ code: 'my', name: 'Burmese', nativeName: 'မြန်မာ' },
	{ code: 'ne', name: 'Nepali', nativeName: 'नेपाली' },
	{ code: 'nl', name: 'Dutch', nativeName: 'Nederlands' },
	{ code: 'no', name: 'Norwegian', nativeName: 'Norsk' },
	{ code: 'pl', name: 'Polish', nativeName: 'Polski' },
	{ code: 'ps', name: 'Pashto', nativeName: 'پښتو' },
	{ code: 'pt', name: 'Portuguese', nativeName: 'Português' },
	{ code: 'ro', name: 'Romanian', nativeName: 'Română' },
	{ code: 'ru', name: 'Russian', nativeName: 'Русский' },
	{ code: 'rw', name: 'Kinyarwanda', nativeName: 'Ikinyarwanda' },
	{ code: 'si', name: 'Sinhala', nativeName: 'සිංහල' },
	{ code: 'sk', name: 'Slovak', nativeName: 'Slovenčina' },
	{ code: 'sl', name: 'Slovenian', nativeName: 'Slovenščina' },
	{ code: 'sm', name: 'Samoan', nativeName: 'Gagana Samoa' },
	{ code: 'so', name: 'Somali', nativeName: 'Soomaali' },
	{ code: 'sq', name: 'Albanian', nativeName: 'Shqip' },
	{ code: 'sr', name: 'Serbian', nativeName: 'Српски' },
	{ code: 'sv', name: 'Swedish', nativeName: 'Svenska' },
	{ code: 'sw', name: 'Swahili', nativeName: 'Kiswahili' },
	{ code: 'tg', name: 'Tajik', nativeName: 'Тоҷикӣ' },
	{ code: 'th', name: 'Thai', nativeName: 'ไทย' },
	{ code: 'ti', name: 'Tigrinya', nativeName: 'ትግርኛ' },
	{ code: 'tk', name: 'Turkmen', nativeName: 'Türkmen' },
	{ code: 'tl', name: 'Filipino', nativeName: 'Tagalog' },
	{ code: 'to', name: 'Tongan', nativeName: 'Lea Fakatonga' },
	{ code: 'tr', name: 'Turkish', nativeName: 'Türkçe' },
	{ code: 'uk', name: 'Ukrainian', nativeName: 'Українська' },
	{ code: 'ur', name: 'Urdu', nativeName: 'اردو' },
	{ code: 'uz', name: 'Uzbek', nativeName: 'Oʻzbek' },
	{ code: 'vi', name: 'Vietnamese', nativeName: 'Tiếng Việt' },
	{ code: 'zh', name: 'Chinese', nativeName: '中文' }
];

// Get language by ISO 639-1 code
export function getLanguage(code: string): LanguageDef | undefined {
	return languages.find((lang) => lang.code === code.toLowerCase());
}

// Search languages by name or code
export function searchLanguages(query: string, limit = 10): LanguageDef[] {
	const q = query.toLowerCase().trim();
	if (!q) return languages.slice(0, limit);

	return languages
		.filter(
			(lang) =>
				lang.name.toLowerCase().includes(q) ||
				lang.nativeName.toLowerCase().includes(q) ||
				lang.code === q
		)
		.sort((a, b) => {
			// Prioritize matches at the start of the name
			const aStarts = a.name.toLowerCase().startsWith(q);
			const bStarts = b.name.toLowerCase().startsWith(q);
			if (aStarts && !bStarts) return -1;
			if (!aStarts && bStarts) return 1;
			return a.name.localeCompare(b.name);
		})
		.slice(0, limit);
}
