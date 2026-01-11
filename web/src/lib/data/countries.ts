// Country repository with useful metadata for localization and forms
// Each country has ISO codes, language, and phone code

export interface CountryDef {
	/** Country name in English */
	name: string;
	/** ISO 3166-1 alpha-2 code (2-letter) */
	code: string;
	/** ISO 3166-1 alpha-3 code (3-letter) */
	code3: string;
	/** Primary language ISO 639-1 code */
	language: string;
	/** Phone country code (without +) */
	phoneCode: string;
}

export const countries: CountryDef[] = [
	{ name: 'Afghanistan', code: 'AF', code3: 'AFG', language: 'ps', phoneCode: '93' },
	{ name: 'Albania', code: 'AL', code3: 'ALB', language: 'sq', phoneCode: '355' },
	{ name: 'Algeria', code: 'DZ', code3: 'DZA', language: 'ar', phoneCode: '213' },
	{ name: 'Andorra', code: 'AD', code3: 'AND', language: 'ca', phoneCode: '376' },
	{ name: 'Angola', code: 'AO', code3: 'AGO', language: 'pt', phoneCode: '244' },
	{ name: 'Antigua and Barbuda', code: 'AG', code3: 'ATG', language: 'en', phoneCode: '1268' },
	{ name: 'Argentina', code: 'AR', code3: 'ARG', language: 'es', phoneCode: '54' },
	{ name: 'Armenia', code: 'AM', code3: 'ARM', language: 'hy', phoneCode: '374' },
	{ name: 'Australia', code: 'AU', code3: 'AUS', language: 'en', phoneCode: '61' },
	{ name: 'Austria', code: 'AT', code3: 'AUT', language: 'de', phoneCode: '43' },
	{ name: 'Azerbaijan', code: 'AZ', code3: 'AZE', language: 'az', phoneCode: '994' },
	{ name: 'Bahamas', code: 'BS', code3: 'BHS', language: 'en', phoneCode: '1242' },
	{ name: 'Bahrain', code: 'BH', code3: 'BHR', language: 'ar', phoneCode: '973' },
	{ name: 'Bangladesh', code: 'BD', code3: 'BGD', language: 'bn', phoneCode: '880' },
	{ name: 'Barbados', code: 'BB', code3: 'BRB', language: 'en', phoneCode: '1246' },
	{ name: 'Belarus', code: 'BY', code3: 'BLR', language: 'be', phoneCode: '375' },
	{ name: 'Belgium', code: 'BE', code3: 'BEL', language: 'nl', phoneCode: '32' },
	{ name: 'Belize', code: 'BZ', code3: 'BLZ', language: 'en', phoneCode: '501' },
	{ name: 'Benin', code: 'BJ', code3: 'BEN', language: 'fr', phoneCode: '229' },
	{ name: 'Bhutan', code: 'BT', code3: 'BTN', language: 'dz', phoneCode: '975' },
	{ name: 'Bolivia', code: 'BO', code3: 'BOL', language: 'es', phoneCode: '591' },
	{ name: 'Bosnia and Herzegovina', code: 'BA', code3: 'BIH', language: 'bs', phoneCode: '387' },
	{ name: 'Botswana', code: 'BW', code3: 'BWA', language: 'en', phoneCode: '267' },
	{ name: 'Brazil', code: 'BR', code3: 'BRA', language: 'pt', phoneCode: '55' },
	{ name: 'Brunei', code: 'BN', code3: 'BRN', language: 'ms', phoneCode: '673' },
	{ name: 'Bulgaria', code: 'BG', code3: 'BGR', language: 'bg', phoneCode: '359' },
	{ name: 'Burkina Faso', code: 'BF', code3: 'BFA', language: 'fr', phoneCode: '226' },
	{ name: 'Burundi', code: 'BI', code3: 'BDI', language: 'fr', phoneCode: '257' },
	{ name: 'Cambodia', code: 'KH', code3: 'KHM', language: 'km', phoneCode: '855' },
	{ name: 'Cameroon', code: 'CM', code3: 'CMR', language: 'fr', phoneCode: '237' },
	{ name: 'Canada', code: 'CA', code3: 'CAN', language: 'en', phoneCode: '1' },
	{ name: 'Cape Verde', code: 'CV', code3: 'CPV', language: 'pt', phoneCode: '238' },
	{ name: 'Central African Republic', code: 'CF', code3: 'CAF', language: 'fr', phoneCode: '236' },
	{ name: 'Chad', code: 'TD', code3: 'TCD', language: 'fr', phoneCode: '235' },
	{ name: 'Chile', code: 'CL', code3: 'CHL', language: 'es', phoneCode: '56' },
	{ name: 'China', code: 'CN', code3: 'CHN', language: 'zh', phoneCode: '86' },
	{ name: 'Colombia', code: 'CO', code3: 'COL', language: 'es', phoneCode: '57' },
	{ name: 'Comoros', code: 'KM', code3: 'COM', language: 'ar', phoneCode: '269' },
	{ name: 'Congo (Democratic Republic)', code: 'CD', code3: 'COD', language: 'fr', phoneCode: '243' },
	{ name: 'Congo (Republic)', code: 'CG', code3: 'COG', language: 'fr', phoneCode: '242' },
	{ name: 'Costa Rica', code: 'CR', code3: 'CRI', language: 'es', phoneCode: '506' },
	{ name: 'Croatia', code: 'HR', code3: 'HRV', language: 'hr', phoneCode: '385' },
	{ name: 'Cuba', code: 'CU', code3: 'CUB', language: 'es', phoneCode: '53' },
	{ name: 'Cyprus', code: 'CY', code3: 'CYP', language: 'el', phoneCode: '357' },
	{ name: 'Czech Republic', code: 'CZ', code3: 'CZE', language: 'cs', phoneCode: '420' },
	{ name: 'Denmark', code: 'DK', code3: 'DNK', language: 'da', phoneCode: '45' },
	{ name: 'Djibouti', code: 'DJ', code3: 'DJI', language: 'fr', phoneCode: '253' },
	{ name: 'Dominica', code: 'DM', code3: 'DMA', language: 'en', phoneCode: '1767' },
	{ name: 'Dominican Republic', code: 'DO', code3: 'DOM', language: 'es', phoneCode: '1809' },
	{ name: 'Ecuador', code: 'EC', code3: 'ECU', language: 'es', phoneCode: '593' },
	{ name: 'Egypt', code: 'EG', code3: 'EGY', language: 'ar', phoneCode: '20' },
	{ name: 'El Salvador', code: 'SV', code3: 'SLV', language: 'es', phoneCode: '503' },
	{ name: 'Equatorial Guinea', code: 'GQ', code3: 'GNQ', language: 'es', phoneCode: '240' },
	{ name: 'Eritrea', code: 'ER', code3: 'ERI', language: 'ti', phoneCode: '291' },
	{ name: 'Estonia', code: 'EE', code3: 'EST', language: 'et', phoneCode: '372' },
	{ name: 'Eswatini', code: 'SZ', code3: 'SWZ', language: 'en', phoneCode: '268' },
	{ name: 'Ethiopia', code: 'ET', code3: 'ETH', language: 'am', phoneCode: '251' },
	{ name: 'Fiji', code: 'FJ', code3: 'FJI', language: 'en', phoneCode: '679' },
	{ name: 'Finland', code: 'FI', code3: 'FIN', language: 'fi', phoneCode: '358' },
	{ name: 'France', code: 'FR', code3: 'FRA', language: 'fr', phoneCode: '33' },
	{ name: 'Gabon', code: 'GA', code3: 'GAB', language: 'fr', phoneCode: '241' },
	{ name: 'Gambia', code: 'GM', code3: 'GMB', language: 'en', phoneCode: '220' },
	{ name: 'Georgia', code: 'GE', code3: 'GEO', language: 'ka', phoneCode: '995' },
	{ name: 'Germany', code: 'DE', code3: 'DEU', language: 'de', phoneCode: '49' },
	{ name: 'Ghana', code: 'GH', code3: 'GHA', language: 'en', phoneCode: '233' },
	{ name: 'Greece', code: 'GR', code3: 'GRC', language: 'el', phoneCode: '30' },
	{ name: 'Grenada', code: 'GD', code3: 'GRD', language: 'en', phoneCode: '1473' },
	{ name: 'Guatemala', code: 'GT', code3: 'GTM', language: 'es', phoneCode: '502' },
	{ name: 'Guinea', code: 'GN', code3: 'GIN', language: 'fr', phoneCode: '224' },
	{ name: 'Guinea-Bissau', code: 'GW', code3: 'GNB', language: 'pt', phoneCode: '245' },
	{ name: 'Guyana', code: 'GY', code3: 'GUY', language: 'en', phoneCode: '592' },
	{ name: 'Haiti', code: 'HT', code3: 'HTI', language: 'fr', phoneCode: '509' },
	{ name: 'Honduras', code: 'HN', code3: 'HND', language: 'es', phoneCode: '504' },
	{ name: 'Hungary', code: 'HU', code3: 'HUN', language: 'hu', phoneCode: '36' },
	{ name: 'Iceland', code: 'IS', code3: 'ISL', language: 'is', phoneCode: '354' },
	{ name: 'India', code: 'IN', code3: 'IND', language: 'hi', phoneCode: '91' },
	{ name: 'Indonesia', code: 'ID', code3: 'IDN', language: 'id', phoneCode: '62' },
	{ name: 'Iran', code: 'IR', code3: 'IRN', language: 'fa', phoneCode: '98' },
	{ name: 'Iraq', code: 'IQ', code3: 'IRQ', language: 'ar', phoneCode: '964' },
	{ name: 'Ireland', code: 'IE', code3: 'IRL', language: 'en', phoneCode: '353' },
	{ name: 'Israel', code: 'IL', code3: 'ISR', language: 'he', phoneCode: '972' },
	{ name: 'Italy', code: 'IT', code3: 'ITA', language: 'it', phoneCode: '39' },
	{ name: 'Ivory Coast', code: 'CI', code3: 'CIV', language: 'fr', phoneCode: '225' },
	{ name: 'Jamaica', code: 'JM', code3: 'JAM', language: 'en', phoneCode: '1876' },
	{ name: 'Japan', code: 'JP', code3: 'JPN', language: 'ja', phoneCode: '81' },
	{ name: 'Jordan', code: 'JO', code3: 'JOR', language: 'ar', phoneCode: '962' },
	{ name: 'Kazakhstan', code: 'KZ', code3: 'KAZ', language: 'kk', phoneCode: '7' },
	{ name: 'Kenya', code: 'KE', code3: 'KEN', language: 'sw', phoneCode: '254' },
	{ name: 'Kiribati', code: 'KI', code3: 'KIR', language: 'en', phoneCode: '686' },
	{ name: 'Kosovo', code: 'XK', code3: 'XKX', language: 'sq', phoneCode: '383' },
	{ name: 'Kuwait', code: 'KW', code3: 'KWT', language: 'ar', phoneCode: '965' },
	{ name: 'Kyrgyzstan', code: 'KG', code3: 'KGZ', language: 'ky', phoneCode: '996' },
	{ name: 'Laos', code: 'LA', code3: 'LAO', language: 'lo', phoneCode: '856' },
	{ name: 'Latvia', code: 'LV', code3: 'LVA', language: 'lv', phoneCode: '371' },
	{ name: 'Lebanon', code: 'LB', code3: 'LBN', language: 'ar', phoneCode: '961' },
	{ name: 'Lesotho', code: 'LS', code3: 'LSO', language: 'en', phoneCode: '266' },
	{ name: 'Liberia', code: 'LR', code3: 'LBR', language: 'en', phoneCode: '231' },
	{ name: 'Libya', code: 'LY', code3: 'LBY', language: 'ar', phoneCode: '218' },
	{ name: 'Liechtenstein', code: 'LI', code3: 'LIE', language: 'de', phoneCode: '423' },
	{ name: 'Lithuania', code: 'LT', code3: 'LTU', language: 'lt', phoneCode: '370' },
	{ name: 'Luxembourg', code: 'LU', code3: 'LUX', language: 'lb', phoneCode: '352' },
	{ name: 'Madagascar', code: 'MG', code3: 'MDG', language: 'mg', phoneCode: '261' },
	{ name: 'Malawi', code: 'MW', code3: 'MWI', language: 'en', phoneCode: '265' },
	{ name: 'Malaysia', code: 'MY', code3: 'MYS', language: 'ms', phoneCode: '60' },
	{ name: 'Maldives', code: 'MV', code3: 'MDV', language: 'dv', phoneCode: '960' },
	{ name: 'Mali', code: 'ML', code3: 'MLI', language: 'fr', phoneCode: '223' },
	{ name: 'Malta', code: 'MT', code3: 'MLT', language: 'mt', phoneCode: '356' },
	{ name: 'Marshall Islands', code: 'MH', code3: 'MHL', language: 'en', phoneCode: '692' },
	{ name: 'Mauritania', code: 'MR', code3: 'MRT', language: 'ar', phoneCode: '222' },
	{ name: 'Mauritius', code: 'MU', code3: 'MUS', language: 'en', phoneCode: '230' },
	{ name: 'Mexico', code: 'MX', code3: 'MEX', language: 'es', phoneCode: '52' },
	{ name: 'Micronesia', code: 'FM', code3: 'FSM', language: 'en', phoneCode: '691' },
	{ name: 'Moldova', code: 'MD', code3: 'MDA', language: 'ro', phoneCode: '373' },
	{ name: 'Monaco', code: 'MC', code3: 'MCO', language: 'fr', phoneCode: '377' },
	{ name: 'Mongolia', code: 'MN', code3: 'MNG', language: 'mn', phoneCode: '976' },
	{ name: 'Montenegro', code: 'ME', code3: 'MNE', language: 'sr', phoneCode: '382' },
	{ name: 'Morocco', code: 'MA', code3: 'MAR', language: 'ar', phoneCode: '212' },
	{ name: 'Mozambique', code: 'MZ', code3: 'MOZ', language: 'pt', phoneCode: '258' },
	{ name: 'Myanmar', code: 'MM', code3: 'MMR', language: 'my', phoneCode: '95' },
	{ name: 'Namibia', code: 'NA', code3: 'NAM', language: 'en', phoneCode: '264' },
	{ name: 'Nauru', code: 'NR', code3: 'NRU', language: 'en', phoneCode: '674' },
	{ name: 'Nepal', code: 'NP', code3: 'NPL', language: 'ne', phoneCode: '977' },
	{ name: 'Netherlands', code: 'NL', code3: 'NLD', language: 'nl', phoneCode: '31' },
	{ name: 'New Zealand', code: 'NZ', code3: 'NZL', language: 'en', phoneCode: '64' },
	{ name: 'Nicaragua', code: 'NI', code3: 'NIC', language: 'es', phoneCode: '505' },
	{ name: 'Niger', code: 'NE', code3: 'NER', language: 'fr', phoneCode: '227' },
	{ name: 'Nigeria', code: 'NG', code3: 'NGA', language: 'en', phoneCode: '234' },
	{ name: 'North Korea', code: 'KP', code3: 'PRK', language: 'ko', phoneCode: '850' },
	{ name: 'North Macedonia', code: 'MK', code3: 'MKD', language: 'mk', phoneCode: '389' },
	{ name: 'Norway', code: 'NO', code3: 'NOR', language: 'no', phoneCode: '47' },
	{ name: 'Oman', code: 'OM', code3: 'OMN', language: 'ar', phoneCode: '968' },
	{ name: 'Pakistan', code: 'PK', code3: 'PAK', language: 'ur', phoneCode: '92' },
	{ name: 'Palau', code: 'PW', code3: 'PLW', language: 'en', phoneCode: '680' },
	{ name: 'Palestine', code: 'PS', code3: 'PSE', language: 'ar', phoneCode: '970' },
	{ name: 'Panama', code: 'PA', code3: 'PAN', language: 'es', phoneCode: '507' },
	{ name: 'Papua New Guinea', code: 'PG', code3: 'PNG', language: 'en', phoneCode: '675' },
	{ name: 'Paraguay', code: 'PY', code3: 'PRY', language: 'es', phoneCode: '595' },
	{ name: 'Peru', code: 'PE', code3: 'PER', language: 'es', phoneCode: '51' },
	{ name: 'Philippines', code: 'PH', code3: 'PHL', language: 'tl', phoneCode: '63' },
	{ name: 'Poland', code: 'PL', code3: 'POL', language: 'pl', phoneCode: '48' },
	{ name: 'Portugal', code: 'PT', code3: 'PRT', language: 'pt', phoneCode: '351' },
	{ name: 'Qatar', code: 'QA', code3: 'QAT', language: 'ar', phoneCode: '974' },
	{ name: 'Romania', code: 'RO', code3: 'ROU', language: 'ro', phoneCode: '40' },
	{ name: 'Russia', code: 'RU', code3: 'RUS', language: 'ru', phoneCode: '7' },
	{ name: 'Rwanda', code: 'RW', code3: 'RWA', language: 'rw', phoneCode: '250' },
	{ name: 'Saint Kitts and Nevis', code: 'KN', code3: 'KNA', language: 'en', phoneCode: '1869' },
	{ name: 'Saint Lucia', code: 'LC', code3: 'LCA', language: 'en', phoneCode: '1758' },
	{ name: 'Saint Vincent and the Grenadines', code: 'VC', code3: 'VCT', language: 'en', phoneCode: '1784' },
	{ name: 'Samoa', code: 'WS', code3: 'WSM', language: 'sm', phoneCode: '685' },
	{ name: 'San Marino', code: 'SM', code3: 'SMR', language: 'it', phoneCode: '378' },
	{ name: 'Sao Tome and Principe', code: 'ST', code3: 'STP', language: 'pt', phoneCode: '239' },
	{ name: 'Saudi Arabia', code: 'SA', code3: 'SAU', language: 'ar', phoneCode: '966' },
	{ name: 'Senegal', code: 'SN', code3: 'SEN', language: 'fr', phoneCode: '221' },
	{ name: 'Serbia', code: 'RS', code3: 'SRB', language: 'sr', phoneCode: '381' },
	{ name: 'Seychelles', code: 'SC', code3: 'SYC', language: 'fr', phoneCode: '248' },
	{ name: 'Sierra Leone', code: 'SL', code3: 'SLE', language: 'en', phoneCode: '232' },
	{ name: 'Singapore', code: 'SG', code3: 'SGP', language: 'en', phoneCode: '65' },
	{ name: 'Slovakia', code: 'SK', code3: 'SVK', language: 'sk', phoneCode: '421' },
	{ name: 'Slovenia', code: 'SI', code3: 'SVN', language: 'sl', phoneCode: '386' },
	{ name: 'Solomon Islands', code: 'SB', code3: 'SLB', language: 'en', phoneCode: '677' },
	{ name: 'Somalia', code: 'SO', code3: 'SOM', language: 'so', phoneCode: '252' },
	{ name: 'South Africa', code: 'ZA', code3: 'ZAF', language: 'en', phoneCode: '27' },
	{ name: 'South Korea', code: 'KR', code3: 'KOR', language: 'ko', phoneCode: '82' },
	{ name: 'South Sudan', code: 'SS', code3: 'SSD', language: 'en', phoneCode: '211' },
	{ name: 'Spain', code: 'ES', code3: 'ESP', language: 'es', phoneCode: '34' },
	{ name: 'Sri Lanka', code: 'LK', code3: 'LKA', language: 'si', phoneCode: '94' },
	{ name: 'Sudan', code: 'SD', code3: 'SDN', language: 'ar', phoneCode: '249' },
	{ name: 'Suriname', code: 'SR', code3: 'SUR', language: 'nl', phoneCode: '597' },
	{ name: 'Sweden', code: 'SE', code3: 'SWE', language: 'sv', phoneCode: '46' },
	{ name: 'Switzerland', code: 'CH', code3: 'CHE', language: 'de', phoneCode: '41' },
	{ name: 'Syria', code: 'SY', code3: 'SYR', language: 'ar', phoneCode: '963' },
	{ name: 'Taiwan', code: 'TW', code3: 'TWN', language: 'zh', phoneCode: '886' },
	{ name: 'Tajikistan', code: 'TJ', code3: 'TJK', language: 'tg', phoneCode: '992' },
	{ name: 'Tanzania', code: 'TZ', code3: 'TZA', language: 'sw', phoneCode: '255' },
	{ name: 'Thailand', code: 'TH', code3: 'THA', language: 'th', phoneCode: '66' },
	{ name: 'Timor-Leste', code: 'TL', code3: 'TLS', language: 'pt', phoneCode: '670' },
	{ name: 'Togo', code: 'TG', code3: 'TGO', language: 'fr', phoneCode: '228' },
	{ name: 'Tonga', code: 'TO', code3: 'TON', language: 'to', phoneCode: '676' },
	{ name: 'Trinidad and Tobago', code: 'TT', code3: 'TTO', language: 'en', phoneCode: '1868' },
	{ name: 'Tunisia', code: 'TN', code3: 'TUN', language: 'ar', phoneCode: '216' },
	{ name: 'Turkey', code: 'TR', code3: 'TUR', language: 'tr', phoneCode: '90' },
	{ name: 'Turkmenistan', code: 'TM', code3: 'TKM', language: 'tk', phoneCode: '993' },
	{ name: 'Tuvalu', code: 'TV', code3: 'TUV', language: 'en', phoneCode: '688' },
	{ name: 'Uganda', code: 'UG', code3: 'UGA', language: 'en', phoneCode: '256' },
	{ name: 'Ukraine', code: 'UA', code3: 'UKR', language: 'uk', phoneCode: '380' },
	{ name: 'United Arab Emirates', code: 'AE', code3: 'ARE', language: 'ar', phoneCode: '971' },
	{ name: 'United Kingdom', code: 'GB', code3: 'GBR', language: 'en', phoneCode: '44' },
	{ name: 'United States', code: 'US', code3: 'USA', language: 'en', phoneCode: '1' },
	{ name: 'Uruguay', code: 'UY', code3: 'URY', language: 'es', phoneCode: '598' },
	{ name: 'Uzbekistan', code: 'UZ', code3: 'UZB', language: 'uz', phoneCode: '998' },
	{ name: 'Vanuatu', code: 'VU', code3: 'VUT', language: 'bi', phoneCode: '678' },
	{ name: 'Vatican City', code: 'VA', code3: 'VAT', language: 'it', phoneCode: '379' },
	{ name: 'Venezuela', code: 'VE', code3: 'VEN', language: 'es', phoneCode: '58' },
	{ name: 'Vietnam', code: 'VN', code3: 'VNM', language: 'vi', phoneCode: '84' },
	{ name: 'Yemen', code: 'YE', code3: 'YEM', language: 'ar', phoneCode: '967' },
	{ name: 'Zambia', code: 'ZM', code3: 'ZMB', language: 'en', phoneCode: '260' },
	{ name: 'Zimbabwe', code: 'ZW', code3: 'ZWE', language: 'en', phoneCode: '263' },
];

// Helper function to search countries
export function searchCountries(query: string, limit = 10): CountryDef[] {
	const q = query.toLowerCase().trim();
	if (!q) return countries.slice(0, limit);

	return countries
		.filter(
			(c) =>
				c.name.toLowerCase().includes(q) ||
				c.code.toLowerCase() === q ||
				c.code3.toLowerCase() === q
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

// Get country by code (2-letter or 3-letter)
export function getCountry(code: string): CountryDef | undefined {
	const c = code.toUpperCase();
	return countries.find((country) => country.code === c || country.code3 === c);
}

// Get country by name
export function getCountryByName(name: string): CountryDef | undefined {
	return countries.find((country) => country.name.toLowerCase() === name.toLowerCase());
}
