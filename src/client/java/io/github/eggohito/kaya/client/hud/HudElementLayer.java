package io.github.eggohito.kaya.client.hud;

import io.github.eggohito.kaya.Kaya;
import io.github.eggohito.kaya.client.hud.internal.HudElementHelperImpl;
import io.github.eggohito.kaya.client.hud.renderer.HudElementRenderer;
import io.github.eggohito.kaya.hud.element.HudElement;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public enum HudElementLayer implements IdentifiedLayer {

	ABOVE_HUD("hud_element/above", HudElement.RenderPhase.ABOVE_HUD),
	BELOW_HUD("hud_element/below", HudElement.RenderPhase.BELOW_HUD);

	private final ResourceLocation id;
	private final HudElement.RenderPhase renderPhase;

	HudElementLayer(String path, HudElement.RenderPhase renderPhase) {
		this.id = Kaya.id(path);
		this.renderPhase = renderPhase;
	}

	@Override
	public ResourceLocation id() {
		return id;
	}

	@Override
	public void render(GuiGraphics graphics, DeltaTracker delta) {

		Player player = Minecraft.getInstance().player;
		Map<HudElement.Type<?>, PriorityQueue<HudElement.WithContext>> batches = new Reference2ObjectArrayMap<>();

		if (player == null) {
			return;
		}

		for (var source : HudElementHelperImpl.getSources()) {

			for (var elementWithContext : source.get(player, renderPhase)) {
				batches
					.computeIfAbsent(elementWithContext.element().getType(), ignored -> new ObjectHeapPriorityQueue<>(HudElement.WithContext::compareTo))
					.enqueue(elementWithContext);
			}

		}

		for (var batch : batches.entrySet()) {

			//noinspection unchecked
			HudElementRenderer<HudElement> renderer = (HudElementRenderer<HudElement>) HudElementHelperImpl.getRenderer(batch.getKey());
			var queued = batch.getValue();

			renderer.init(graphics, delta);

			graphics.pose().pushPose();
			graphics.pose().translate(0.0F, 0.0F, 200.0F);

			while (!queued.isEmpty()) {
				var elementWithContext = queued.dequeue();
				renderer.render(elementWithContext.context(), elementWithContext.element(), graphics, delta);
			}

			graphics.pose().popPose();

		}

	}

}
