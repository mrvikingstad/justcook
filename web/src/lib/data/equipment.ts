// Equipment repository for recipe requirements
// Each equipment item has a key (for DB) and display name
// Note: This list focuses on specialty/uncommon equipment
// Common items like pots, pans, knives are assumed to be available

export interface EquipmentDef {
	key: string;
	name: string;
	category: 'appliances' | 'cookware' | 'bakeware' | 'utensils' | 'specialty';
}

export const equipmentList: EquipmentDef[] = [
	// Appliances
	{ key: 'stand_mixer', name: 'Stand mixer', category: 'appliances' },
	{ key: 'food_processor', name: 'Food processor', category: 'appliances' },
	{ key: 'blender', name: 'Blender', category: 'appliances' },
	{ key: 'immersion_blender', name: 'Immersion blender', category: 'appliances' },
	{ key: 'slow_cooker', name: 'Slow cooker', category: 'appliances' },
	{ key: 'pressure_cooker', name: 'Pressure cooker', category: 'appliances' },
	{ key: 'air_fryer', name: 'Air fryer', category: 'appliances' },
	{ key: 'deep_fryer', name: 'Deep fryer', category: 'appliances' },
	{ key: 'sous_vide', name: 'Sous vide circulator', category: 'appliances' },
	{ key: 'instant_pot', name: 'Instant Pot', category: 'appliances' },
	{ key: 'rice_cooker', name: 'Rice cooker', category: 'appliances' },
	{ key: 'bread_machine', name: 'Bread machine', category: 'appliances' },
	{ key: 'waffle_iron', name: 'Waffle iron', category: 'appliances' },
	{ key: 'ice_cream_maker', name: 'Ice cream maker', category: 'appliances' },
	{ key: 'dehydrator', name: 'Food dehydrator', category: 'appliances' },
	{ key: 'meat_grinder', name: 'Meat grinder', category: 'appliances' },
	{ key: 'pasta_maker', name: 'Pasta maker/roller', category: 'appliances' },
	{ key: 'espresso_machine', name: 'Espresso machine', category: 'appliances' },
	{ key: 'electric_kettle', name: 'Electric kettle', category: 'appliances' },
	{ key: 'hand_mixer', name: 'Hand mixer', category: 'appliances' },
	{ key: 'juicer', name: 'Juicer', category: 'appliances' },
	{ key: 'toaster_oven', name: 'Toaster oven', category: 'appliances' },
	{ key: 'panini_press', name: 'Panini press', category: 'appliances' },
	{ key: 'electric_griddle', name: 'Electric griddle', category: 'appliances' },
	{ key: 'popcorn_maker', name: 'Popcorn maker', category: 'appliances' },
	{ key: 'cotton_candy_machine', name: 'Cotton candy machine', category: 'appliances' },

	// Cookware
	{ key: 'dutch_oven', name: 'Dutch oven', category: 'cookware' },
	{ key: 'cast_iron_skillet', name: 'Cast iron skillet', category: 'cookware' },
	{ key: 'wok', name: 'Wok', category: 'cookware' },
	{ key: 'griddle', name: 'Griddle', category: 'cookware' },
	{ key: 'grill_pan', name: 'Grill pan', category: 'cookware' },
	{ key: 'double_boiler', name: 'Double boiler', category: 'cookware' },
	{ key: 'steamer', name: 'Steamer basket/pot', category: 'cookware' },
	{ key: 'stockpot', name: 'Large stockpot (8+ qt)', category: 'cookware' },
	{ key: 'braiser', name: 'Braiser', category: 'cookware' },
	{ key: 'roasting_pan', name: 'Roasting pan with rack', category: 'cookware' },
	{ key: 'saute_pan', name: 'Sauté pan with lid', category: 'cookware' },
	{ key: 'fish_poacher', name: 'Fish poacher', category: 'cookware' },
	{ key: 'fondue_pot', name: 'Fondue pot', category: 'cookware' },
	{ key: 'chafing_dish', name: 'Chafing dish', category: 'cookware' },

	// Bakeware
	{ key: 'springform_pan', name: 'Springform pan', category: 'bakeware' },
	{ key: 'bundt_pan', name: 'Bundt pan', category: 'bakeware' },
	{ key: 'tart_pan', name: 'Tart pan', category: 'bakeware' },
	{ key: 'pie_dish', name: 'Pie dish', category: 'bakeware' },
	{ key: 'ramekins', name: 'Ramekins', category: 'bakeware' },
	{ key: 'loaf_pan', name: 'Loaf pan', category: 'bakeware' },
	{ key: 'muffin_tin', name: 'Muffin tin', category: 'bakeware' },
	{ key: 'sheet_pan', name: 'Sheet pan/baking sheet', category: 'bakeware' },
	{ key: 'pizza_stone', name: 'Pizza stone', category: 'bakeware' },
	{ key: 'pizza_peel', name: 'Pizza peel', category: 'bakeware' },
	{ key: 'cooling_rack', name: 'Wire cooling rack', category: 'bakeware' },
	{ key: 'cake_pan_round', name: 'Round cake pan', category: 'bakeware' },
	{ key: 'cake_pan_square', name: 'Square cake pan', category: 'bakeware' },
	{ key: 'tube_pan', name: 'Tube pan', category: 'bakeware' },
	{ key: 'jelly_roll_pan', name: 'Jelly roll pan', category: 'bakeware' },
	{ key: 'bread_pan', name: 'Bread pan', category: 'bakeware' },
	{ key: 'baking_dish', name: 'Baking dish (9x13)', category: 'bakeware' },
	{ key: 'souffle_dish', name: 'Soufflé dish', category: 'bakeware' },
	{ key: 'dutch_oven_baking', name: 'Dutch oven (for bread)', category: 'bakeware' },
	{ key: 'silicone_molds', name: 'Silicone baking molds', category: 'bakeware' },
	{ key: 'banneton', name: 'Bread proofing basket (banneton)', category: 'bakeware' },
	{ key: 'bread_lame', name: 'Bread lame/scoring tool', category: 'bakeware' },

	// Utensils
	{ key: 'mandoline', name: 'Mandoline slicer', category: 'utensils' },
	{ key: 'mortar_pestle', name: 'Mortar and pestle', category: 'utensils' },
	{ key: 'kitchen_scale', name: 'Kitchen scale', category: 'utensils' },
	{ key: 'thermometer', name: 'Instant-read thermometer', category: 'utensils' },
	{ key: 'candy_thermometer', name: 'Candy thermometer', category: 'utensils' },
	{ key: 'meat_thermometer', name: 'Meat thermometer', category: 'utensils' },
	{ key: 'oven_thermometer', name: 'Oven thermometer', category: 'utensils' },
	{ key: 'kitchen_torch', name: 'Kitchen torch', category: 'utensils' },
	{ key: 'pastry_brush', name: 'Pastry brush', category: 'utensils' },
	{ key: 'rolling_pin', name: 'Rolling pin', category: 'utensils' },
	{ key: 'pastry_bag', name: 'Pastry bag with tips', category: 'utensils' },
	{ key: 'fine_mesh_sieve', name: 'Fine mesh sieve', category: 'utensils' },
	{ key: 'cheesecloth', name: 'Cheesecloth', category: 'utensils' },
	{ key: 'zester', name: 'Microplane/zester', category: 'utensils' },
	{ key: 'meat_mallet', name: 'Meat mallet/tenderizer', category: 'utensils' },
	{ key: 'citrus_juicer', name: 'Citrus juicer', category: 'utensils' },
	{ key: 'spider_strainer', name: 'Spider strainer', category: 'utensils' },
	{ key: 'bench_scraper', name: 'Bench scraper', category: 'utensils' },
	{ key: 'dough_scraper', name: 'Dough scraper', category: 'utensils' },
	{ key: 'pastry_blender', name: 'Pastry blender', category: 'utensils' },
	{ key: 'cookie_cutters', name: 'Cookie cutters', category: 'utensils' },
	{ key: 'biscuit_cutter', name: 'Biscuit cutter', category: 'utensils' },
	{ key: 'pizza_cutter', name: 'Pizza cutter', category: 'utensils' },
	{ key: 'apple_corer', name: 'Apple corer', category: 'utensils' },
	{ key: 'cherry_pitter', name: 'Cherry pitter', category: 'utensils' },
	{ key: 'garlic_press', name: 'Garlic press', category: 'utensils' },
	{ key: 'potato_masher', name: 'Potato masher', category: 'utensils' },
	{ key: 'potato_ricer', name: 'Potato ricer', category: 'utensils' },
	{ key: 'food_mill', name: 'Food mill', category: 'utensils' },
	{ key: 'salad_spinner', name: 'Salad spinner', category: 'utensils' },
	{ key: 'chinois', name: 'Chinois strainer', category: 'utensils' },
	{ key: 'kitchen_twine', name: 'Kitchen twine', category: 'utensils' },
	{ key: 'meat_injector', name: 'Meat injector', category: 'utensils' },
	{ key: 'basting_brush', name: 'Basting brush', category: 'utensils' },
	{ key: 'oil_sprayer', name: 'Oil sprayer', category: 'utensils' },
	{ key: 'egg_separator', name: 'Egg separator', category: 'utensils' },
	{ key: 'offset_spatula', name: 'Offset spatula', category: 'utensils' },
	{ key: 'cake_turntable', name: 'Cake turntable', category: 'utensils' },
	{ key: 'decorating_comb', name: 'Cake decorating comb', category: 'utensils' },

	// Specialty
	{ key: 'tortilla_press', name: 'Tortilla press', category: 'specialty' },
	{ key: 'dumpling_press', name: 'Dumpling press', category: 'specialty' },
	{ key: 'sushi_mat', name: 'Sushi rolling mat', category: 'specialty' },
	{ key: 'bamboo_steamer', name: 'Bamboo steamer', category: 'specialty' },
	{ key: 'crepe_pan', name: 'Crepe pan', category: 'specialty' },
	{ key: 'tagine', name: 'Tagine', category: 'specialty' },
	{ key: 'paella_pan', name: 'Paella pan', category: 'specialty' },
	{ key: 'smoker_box', name: 'Smoker box', category: 'specialty' },
	{ key: 'jerky_gun', name: 'Jerky gun', category: 'specialty' },
	{ key: 'popover_pan', name: 'Popover pan', category: 'specialty' },
	{ key: 'madeleine_pan', name: 'Madeleine pan', category: 'specialty' },
	{ key: 'cannoli_forms', name: 'Cannoli forms', category: 'specialty' },
	{ key: 'pizzelle_maker', name: 'Pizzelle maker', category: 'specialty' },
	{ key: 'krumkake_iron', name: 'Krumkake iron', category: 'specialty' },
	{ key: 'egg_poacher', name: 'Egg poacher', category: 'specialty' },
	{ key: 'canning_supplies', name: 'Canning jars & supplies', category: 'specialty' },
	{ key: 'ravioli_mold', name: 'Ravioli mold', category: 'specialty' },
	{ key: 'gnocchi_board', name: 'Gnocchi board', category: 'specialty' },
	{ key: 'spaetzle_maker', name: 'Spätzle maker', category: 'specialty' },
	{ key: 'empanada_press', name: 'Empanada press', category: 'specialty' },
	{ key: 'takoyaki_pan', name: 'Takoyaki pan', category: 'specialty' },
	{ key: 'aebleskiver_pan', name: 'Aebleskiver pan', category: 'specialty' },
	{ key: 'poffertjes_pan', name: 'Poffertjes pan', category: 'specialty' },
	{ key: 'churro_maker', name: 'Churro maker', category: 'specialty' },
	{ key: 'donut_pan', name: 'Donut pan', category: 'specialty' },
	{ key: 'cake_pop_mold', name: 'Cake pop mold', category: 'specialty' },
	{ key: 'escargot_dish', name: 'Escargot dish', category: 'specialty' },
	{ key: 'oyster_knife', name: 'Oyster knife', category: 'specialty' },
	{ key: 'lobster_cracker', name: 'Lobster cracker', category: 'specialty' },
	{ key: 'fish_scaler', name: 'Fish scaler', category: 'specialty' },
	{ key: 'moka_pot', name: 'Moka pot', category: 'specialty' },
	{ key: 'french_press', name: 'French press', category: 'specialty' },
	{ key: 'pour_over', name: 'Pour-over coffee maker', category: 'specialty' },
	{ key: 'cheese_grater_rotary', name: 'Rotary cheese grater', category: 'specialty' },
	{ key: 'truffle_slicer', name: 'Truffle slicer', category: 'specialty' },
	{ key: 'spiralizer', name: 'Spiralizer', category: 'specialty' },
	{ key: 'corn_stripper', name: 'Corn stripper', category: 'specialty' },
	{ key: 'avocado_slicer', name: 'Avocado slicer', category: 'specialty' },
	{ key: 'mango_slicer', name: 'Mango slicer', category: 'specialty' },
	{ key: 'egg_slicer', name: 'Egg slicer', category: 'specialty' },
	{ key: 'strawberry_huller', name: 'Strawberry huller', category: 'specialty' },
	{ key: 'herb_stripper', name: 'Herb stripper', category: 'specialty' },
	{ key: 'vacuum_sealer', name: 'Vacuum sealer', category: 'specialty' },
	{ key: 'smoking_gun', name: 'Smoking gun', category: 'specialty' }
];

// Helper function to search equipment
export function searchEquipment(query: string, limit = 10): EquipmentDef[] {
	const q = query.toLowerCase().trim();
	if (!q) return [];

	return equipmentList
		.filter((e) => e.name.toLowerCase().includes(q) || e.key.includes(q))
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

// Get equipment by key
export function getEquipment(key: string): EquipmentDef | undefined {
	return equipmentList.find((e) => e.key === key);
}
