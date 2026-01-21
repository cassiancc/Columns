package io.github.haykam821.columns.data.provider;

import java.util.concurrent.CompletableFuture;

import io.github.haykam821.columns.Main;
import io.github.haykam821.columns.block.ColumnTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.tags.BlockTags;

public class ColumnsBlockTagProvider extends FabricTagProvider.BlockTagProvider {
	public ColumnsBlockTagProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registries) {
		super(dataOutput, registries);
	}

	@Override
	protected void addTags(Provider lookup) {
		FabricTagBuilder builder = this.getOrCreateTagBuilder(Main.COLUMNS_BLOCK_TAG);
		for (ColumnTypes columnType : ColumnTypes.values()) {
			builder.add(columnType.block);
		}

		this.getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_PICKAXE).addTag(Main.COLUMNS_BLOCK_TAG);
	}
}
