package ru.alaverdyan.artem.originstransgender.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FakeExperienceOrbEntity extends ExperienceOrbEntity {
    private Vec3d target;
    private int orbAge;

    public FakeExperienceOrbEntity(EntityType<? extends ExperienceOrbEntity> type, World world) {
        super(type, world);
    }

    public FakeExperienceOrbEntity(EntityType<? extends ExperienceOrbEntity> type, World world,
                                   Vec3d target, double x, double y, double z) {
        super(type, world);
        this.refreshPositionAndAngles(x, y, z, 0, 0);
        this.target = target;
        this.setInvulnerable(true);
    }

    public void setTarget(Vec3d target) {
        this.target = target;
    }

    @Override
    public void tick() {
        baseTick();
        if(target == null) return;
        this.prevX = this.getX();
        this.prevY = this.getY();
        this.prevZ = this.getZ();
        if (this.isSubmergedIn(FluidTags.WATER)) {
        } else if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add((double) 0.0F, -0.03, (double) 0.0F));
        }

        if (this.getWorld().getFluidState(this.getBlockPos()).isIn(FluidTags.LAVA)) {
            this.setVelocity((double) ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F), (double) 0.2F, (double) ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F));
        }

        if (!this.getWorld().isSpaceEmpty(this.getBoundingBox())) {
            this.pushOutOfBlocks(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / (double) 2.0F, this.getZ());
        }

        Vec3d vec3d = new Vec3d(this.target.getX() - this.getX(), this.target.getY() + (double)1.7/ (double)2.0F - this.getY(), this.target.getZ() - this.getZ());
        double d = vec3d.lengthSquared();
        if (d < (double) 64.0F) {
            double e = (double) 1.0F - Math.sqrt(d) / (double) 8.0F;
            this.setVelocity(this.getVelocity().add(vec3d.normalize().multiply(e * e * 0.1)));
        }
        

        this.move(MovementType.SELF, this.getVelocity());
        float f = 0.98F;
        if (this.isOnGround()) {
            f = this.getWorld().getBlockState(this.getVelocityAffectingPos()).getBlock().getSlipperiness() * 0.98F;
        }

        this.setVelocity(this.getVelocity().multiply((double) f, 0.98, (double) f));
        if (this.isOnGround()) {
            this.setVelocity(this.getVelocity().multiply((double) 1.0F, -0.9, (double) 1.0F));
        }

        double distance = getPos().distanceTo(target);
        if (distance <= 1) {
            this.discard();
            return;
        }

        ++this.orbAge;
        if (this.orbAge >= 6000) {
            this.discard();
        }
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
    }
}
