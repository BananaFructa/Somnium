package BananaFructa.somnium.mechanics.projectiles;

import BananaFructa.somnium.Entities;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Pulse extends Projectile {

    ProgrammableProjectileInfo projectileInfo = null;

    int lifetime;

    public Pulse(EntityType<? extends Projectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
    }

    private final IntOpenHashSet ignoredEntities = new IntOpenHashSet();

    public static Pulse make(Level level, LivingEntity shooter, int lifetime) {
        Pulse pulse = new Pulse(Entities.pulseProjectile.get(), level);
        pulse.setOwner(shooter);
        pulse.setPos(
                shooter.getX(),
                shooter.getEyeY() - 0.1,
                shooter.getZ()
        );
        float factor = 10;
        Vec3 look = shooter.getViewVector(1).multiply(0.1*factor,0.1*factor,0.1*factor);
        pulse.setDeltaMovement(look);
        pulse.lifetime = lifetime;
        return pulse;
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 motion = this.getDeltaMovement();
        Vec3 initialPosition = position();
        this.setPos(
                this.getX() + motion.x,
                this.getY() + motion.y,
                this.getZ() + motion.z
        );
        Vec3 finalPosition = position();
        Vec3 direction = getDeltaMovement().normalize().multiply(0.1, 0.1, 0.1);

        if (this.level().isClientSide) return;

        if (--lifetime <= 0) {
            discard();
        }

        TrailParticleOptions type = new TrailParticleOptions(0, 1, 1,0.1f,0,true);
        while (finalPosition.subtract(initialPosition).length() > 0.1) {
            initialPosition = initialPosition.add(direction);
            double x = initialPosition.x + (level().random.nextFloat() * 0.15 - 0.0525);
            double y = initialPosition.y + (level().random.nextFloat() * 0.15 - 0.0525);
            double z = initialPosition.z + (level().random.nextFloat() * 0.15 - 0.0525);
            sendParticles(type, x, y, z, 1, 0, 0, 0, 0);
        }

        double x = getX() + (level().random.nextFloat() * 0.15 - 0.0525);
        double y = getY() + (level().random.nextFloat() * 0.15 - 0.0525);
        double z = getZ() + (level().random.nextFloat() * 0.15 - 0.0525);
        sendParticles(type, x, y, z, 1, 0, 0, 0, 0);
        //

        Vec3 pos = this.position();
        Vec3 next = pos.add(getDeltaMovement());
        HitResult hitresult = this.level().clip(new ClipContext(pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hitresult.getType() != HitResult.Type.MISS) {
            next = hitresult.getLocation();
        }


        EntityHitResult entityhitresult = this.findHitEntity(pos, next);
        if (entityhitresult != null) {
            hitresult = entityhitresult;
        }

        if (hitresult != null && hitresult.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hitresult).getEntity();
            Entity entity1 = this.getOwner();
            if (entity instanceof Player && entity1 instanceof Player && !((Player) entity1).canHarmPlayer((Player) entity)) {
                hitresult = null;
            }
        }

        if (hitresult != null && hitresult.getType() != HitResult.Type.MISS) {
            var result = net.minecraftforge.event.ForgeEventFactory.onProjectileImpactResultNullable(this, hitresult);
            if (result == null) {
                if (hitresult.getType() != HitResult.Type.ENTITY) return;
                result = net.minecraftforge.event.entity.ProjectileImpactEvent.ImpactResult.SKIP_ENTITY;
            }
            switch (result) {
                case SKIP_ENTITY:
                    if (hitresult.getType() != HitResult.Type.ENTITY) { // If there is no entity, we just return default behaviour
                        this.onHit(hitresult);
                        this.hasImpulse = true;
                        break;
                    }
                    break;
                case STOP_AT_CURRENT_NO_DAMAGE:
                    this.discard();
                    break;
                case STOP_AT_CURRENT:
                case DEFAULT:
                    this.onHit(hitresult);
                    this.hasImpulse = true;
                    break;
            }
        }
    }

    protected EntityHitResult findHitEntity(Vec3 p_36758_, Vec3 p_36759_) {
        return ProjectileUtil.getEntityHitResult(this.level(), this, p_36758_, p_36759_, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), this::canHitEntity);
    }

    public <T extends ParticleOptions> int sendParticles(T p_8768_, double p_8769_, double p_8770_, double p_8771_, int p_8772_, double p_8773_, double p_8774_, double p_8775_, double p_8776_) {
        ClientboundLevelParticlesPacket clientboundlevelparticlespacket = new ClientboundLevelParticlesPacket(p_8768_, true, p_8769_, p_8770_, p_8771_, (float)p_8773_, (float)p_8774_, (float)p_8775_, (float)p_8776_, p_8772_);
        int i = 0;

        for(int j = 0; j < ((ServerLevel)level()).players().size(); ++j) {
            ServerPlayer serverplayer = ((ServerLevel)level()).players().get(j);
            if (sendParticles(serverplayer, true, p_8769_, p_8770_, p_8771_, clientboundlevelparticlespacket)) {
                ++i;
            }
        }

        return i;
    }

    private void collisionSpread() {
        TrailParticleOptions type = new TrailParticleOptions(0, 1, 1,0.05f,1,false);
        double speed = getDeltaMovement().length()*5;
        for (int i = 0;i < 30;i++) {
            double x = getX() + (level().random.nextFloat() * 0.15 - 0.0525);
            double y = getY() + (level().random.nextFloat() * 0.15 - 0.0525);
            double z = getZ() + (level().random.nextFloat() * 0.15 - 0.0525);
            sendParticles(type, x, y, z, 1, (level().random.nextFloat() * 2 - 1) * speed, (level().random.nextFloat() * 2 - 1) * speed, (level().random.nextFloat() * 2 - 1) * speed, 0);
        }
    }

    private boolean sendParticles(ServerPlayer p_8637_, boolean p_8638_, double p_8639_, double p_8640_, double p_8641_, Packet<?> p_8642_) {
        if (p_8637_.level() != this.level()) {
            return false;
        } else {
            BlockPos blockpos = p_8637_.blockPosition();
            if (blockpos.closerToCenterThan(new Vec3(p_8639_, p_8640_, p_8641_), p_8638_ ? 512.0D : 32.0D)) {
                p_8637_.connection.send(p_8642_);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide) {
            result.getEntity().hurt(
                    damageSources().thrown(this, this.getOwner()), 6.0F
            );
            collisionSpread();
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!level().isClientSide) {
            collisionSpread();
            this.discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("lifetime",lifetime);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        lifetime = tag.getInt("lifetime");
    }

    @Override
    protected void defineSynchedData() {

    }
}
