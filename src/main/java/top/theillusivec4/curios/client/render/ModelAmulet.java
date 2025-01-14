package top.theillusivec4.curios.client.render;

import net.minecraft.client.renderer.entity.model.ModelBase;
import net.minecraft.client.renderer.entity.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelAmulet extends ModelBase {

    public ModelRenderer amulet;

    public ModelAmulet() {
        this.textureWidth = 16;
        this.textureHeight = 16;
        this.amulet = new ModelRenderer(this, 0, 0);
        this.amulet.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.amulet.addBox(-2.0F, 2.0F, -3.0F, 4, 4, 1, 0.0F);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.amulet.render(scale);
    }
}
