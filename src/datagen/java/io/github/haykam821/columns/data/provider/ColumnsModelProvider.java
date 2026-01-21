package io.github.haykam821.columns.data.provider;

import java.util.Optional;

import io.github.haykam821.columns.Main;
import io.github.haykam821.columns.block.ColumnBlock;
import io.github.haykam821.columns.block.ColumnTypes;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.BlockStateVariant;
import net.minecraft.client.data.MultipartBlockStateSupplier;
import net.minecraft.client.data.VariantSettings;
import net.minecraft.client.data.When;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ColumnsModelProvider extends FabricModelProvider {
	public static final ModelTemplate COLUMN_CENTER = createModel("column_center", "_center", TextureSlot.ALL);
	public static final ModelTemplate COLUMN_END = createModel("column_end", "_end", TextureSlot.ALL);
	public static final ModelTemplate COLUMN_INVENTORY = createModel("column_inventory", "_inventory", TextureSlot.ALL);

	public ColumnsModelProvider(FabricDataOutput dataOutput) {
		super(dataOutput);
	}

	@Override
	public void generateBlockStateModels(BlockModelGenerators modelGenerator) {
		for (ColumnTypes columnType : ColumnTypes.values()) {
			ColumnsModelProvider.registerColumn(modelGenerator, columnType.block, columnType.base);
		}
	}

	@Override
	public void generateItemModels(ItemModelGenerators modelGenerator) {
		return;
	}

	public static void registerColumn(BlockModelGenerators modelGenerator, Block block, Block base) {
		TextureMapping textures = getTextures(base);

		Identifier centerId = COLUMN_CENTER.create(block, textures, modelGenerator.modelOutput);
		Identifier endId = COLUMN_END.create(block, textures, modelGenerator.modelOutput);

		modelGenerator.blockStateOutput.accept(MultipartBlockStateSupplier.create(block)
			.with(createVariant(centerId))
			.with(When.create().set(ColumnBlock.DOWN, true), createVariant(endId))
			.with(When.create().set(ColumnBlock.UP, true), createVariantRotated(endId)));

		Identifier inventoryId = COLUMN_INVENTORY.create(block, textures, modelGenerator.modelOutput);
		modelGenerator.registerSimpleItemModel(block, inventoryId);
	}

	private static TextureMapping getTextures(Block base) {
		if (base == Blocks.SANDSTONE || base == Blocks.RED_SANDSTONE) {
			return TextureMapping.cube(TextureMapping.getBlockTexture(base, "_top"));
		}
		return TextureMapping.cube(base);
	}

	private static BlockStateVariant createVariant(Identifier modelId) {
		return BlockStateVariant.create().put(VariantSettings.MODEL, modelId);
	}

	private static BlockStateVariant createVariantRotated(Identifier modelId) {
		return createVariant(modelId)
			.put(VariantSettings.X, VariantSettings.Rotation.R180)
			.put(VariantSettings.UVLOCK, true);
	}

	private static ModelTemplate createModel(String parent, String variant, TextureSlot... requiredTextures) {
		return new ModelTemplate(Optional.of(Main.id("block/" + parent)), Optional.of(variant), requiredTextures);
	}
}
