package io.github.haykam821.columns.data.provider;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import io.github.haykam821.columns.block.ColumnTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementRequirements.CriterionMerger;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.recipe.StonecuttingRecipeJsonBuilder;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.SingleStackRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public class ColumnsRecipeGenerator extends RecipeGenerator {
	protected ColumnsRecipeGenerator(RegistryWrapper.WrapperLookup registries, RecipeExporter exporter) {
		super(registries, exporter);
	}

	@Override
	public void generate() {
		for (ColumnTypes columnType : ColumnTypes.values()) {
			this.offerColumnRecipe(columnType.block, columnType.base);
			this.offerColumnStonecuttingRecipe(columnType.block, columnType.base);
		}

		this.offerCustomColumnStonecuttingRecipe(ColumnTypes.DEEPSLATE_BRICK.block, Blocks.COBBLED_DEEPSLATE);
		this.offerCustomColumnStonecuttingRecipe(ColumnTypes.DEEPSLATE_BRICK.block, Blocks.POLISHED_DEEPSLATE);

		this.offerCustomColumnStonecuttingRecipe(ColumnTypes.DEEPSLATE_TILE.block, Blocks.COBBLED_DEEPSLATE);
		this.offerCustomColumnStonecuttingRecipe(ColumnTypes.DEEPSLATE_TILE.block, Blocks.DEEPSLATE_BRICKS);
		this.offerCustomColumnStonecuttingRecipe(ColumnTypes.DEEPSLATE_TILE.block, Blocks.POLISHED_DEEPSLATE);

		this.offerCustomColumnStonecuttingRecipe(ColumnTypes.END_STONE_BRICK.block, Blocks.END_STONE);

		this.offerCustomColumnStonecuttingRecipe(ColumnTypes.POLISHED_BLACKSTONE_BRICK.block, Blocks.BLACKSTONE);
		this.offerCustomColumnStonecuttingRecipe(ColumnTypes.POLISHED_BLACKSTONE_BRICK.block, Blocks.POLISHED_BLACKSTONE);

		this.offerCustomColumnStonecuttingRecipe(ColumnTypes.POLISHED_BLACKSTONE.block, Blocks.BLACKSTONE);

		this.offerCustomColumnStonecuttingRecipe(ColumnTypes.POLISHED_DEEPSLATE.block, Blocks.COBBLED_DEEPSLATE);

		this.offerCustomColumnStonecuttingRecipe(ColumnTypes.POLISHED_TUFF.block, Blocks.TUFF);

		this.offerCustomColumnStonecuttingRecipe(ColumnTypes.STONE_BRICK.block, Blocks.STONE);

		this.offerCustomColumnStonecuttingRecipe(ColumnTypes.TUFF_BRICK.block, Blocks.TUFF);
		this.offerCustomColumnStonecuttingRecipe(ColumnTypes.TUFF_BRICK.block, Blocks.POLISHED_TUFF);
	}

	public void offerColumnRecipe(Block block, Block base) {
		this.offerCraftingTo(this.getColumnRecipe(block, Ingredient.ofItems(base))
			.criterion(RecipeGenerator.hasItem(base), this.conditionsFromItem(base)));
	}

	public ShapedRecipeJsonBuilder getColumnRecipe(ItemConvertible output, Ingredient input) {
		return this.createShaped(RecipeCategory.DECORATIONS, output, 6)
			.input('#', input)
			.pattern("###")
			.pattern(" # ")
			.pattern("###");
	}

	public void offerColumnStonecuttingRecipe(Block block, Block base) {
		Identifier blockId = Registries.ITEM.getId(block.asItem());

		Identifier recipeId = blockId.withSuffixedPath("_from_stonecutting");
		RegistryKey<Recipe<?>> recipeKey = RegistryKey.of(RegistryKeys.RECIPE, recipeId);

		this.offerCustomColumnStonecuttingRecipe(recipeKey, block, base);
	}

	public void offerCustomColumnStonecuttingRecipe(Block block, Block base) {
		Identifier baseId = Registries.ITEM.getId(base.asItem());
		Identifier blockId = Registries.ITEM.getId(block.asItem());

		Identifier recipeId = blockId.withPath(path -> path + "_from_" + baseId.getPath() + "_stonecutting");
		RegistryKey<Recipe<?>> recipeKey = RegistryKey.of(RegistryKeys.RECIPE, recipeId);

		this.offerCustomColumnStonecuttingRecipe(recipeKey, block, base);
	}

	private void offerCustomColumnStonecuttingRecipe(RegistryKey<Recipe<?>> recipeKey, Block block, Block base) {
		this.offerSingleItemTo(recipeKey, StonecuttingRecipeJsonBuilder.createStonecutting(Ingredient.ofItems(base), RecipeCategory.DECORATIONS, block, 1)
			.criterion(RecipeGenerator.hasItem(base), this.conditionsFromItem(base)));
	}

	private void offerCraftingTo(ShapedRecipeJsonBuilder factory) {
		Identifier recipeId = CraftingRecipeJsonBuilder.getItemId(factory.getOutputItem());
		RegistryKey<Recipe<?>> recipeKey = RegistryKey.of(RegistryKeys.RECIPE, recipeId);

		this.offerShapedTo(recipeKey, factory);
	}

	private void offerShapedTo(RegistryKey<Recipe<?>> recipeKey, ShapedRecipeJsonBuilder factory) {
		RawShapedRecipe rawRecipe = factory.validate(recipeKey);

		Identifier advancementId = ColumnsRecipeGenerator.getAdvancementId(recipeKey);
		Advancement.Builder advancementBuilder = this.exporter.getAdvancementBuilder()
			.criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeKey))
			.rewards(AdvancementRewards.Builder.recipe(recipeKey))
			.criteriaMerger(CriterionMerger.OR);

		factory.criteria.forEach(advancementBuilder::criterion);

		AdvancementEntry advancement = advancementBuilder.build(advancementId);

		String group = Objects.requireNonNullElse(factory.group, "");
		CraftingRecipeCategory category = CraftingRecipeJsonBuilder.toCraftingCategory(factory.category);
		ItemStack output = new ItemStack(factory.getOutputItem(), factory.count);

		ShapedRecipe recipe = new ShapedRecipe(group, category, rawRecipe, output, factory.showNotification);
		this.exporter.accept(recipeKey, recipe, advancement);
	}

	private void offerSingleItemTo(RegistryKey<Recipe<?>> recipeKey, StonecuttingRecipeJsonBuilder factory) {
		factory.validate(recipeKey);

		Identifier advancementId = ColumnsRecipeGenerator.getAdvancementId(recipeKey);
		Advancement.Builder advancementBuilder = this.exporter.getAdvancementBuilder()
			.criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeKey))
			.rewards(AdvancementRewards.Builder.recipe(recipeKey))
			.criteriaMerger(CriterionMerger.OR);

		factory.criteria.forEach(advancementBuilder::criterion);

		AdvancementEntry advancement = advancementBuilder.build(advancementId);

		String group = Objects.requireNonNullElse(factory.group, "");
		ItemStack output = new ItemStack(factory.getOutputItem(), factory.count);

		SingleStackRecipe recipe = factory.recipeFactory.create(group, factory.input, output);
		this.exporter.accept(recipeKey, recipe, advancement);
	}

	private static Identifier getAdvancementId(RegistryKey<Recipe<?>> recipeKey) {
		return recipeKey.getValue().withPrefixedPath("recipes/");
	}

	public static class Provider extends FabricRecipeProvider {
		public Provider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registries) {
			super(dataOutput, registries);
		}

		@Override
		protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registries, RecipeExporter exporter) {
			return new ColumnsRecipeGenerator(registries, exporter);
		}

		@Override
		public String getName() {
			return "Recipes";
		}
	}
}
