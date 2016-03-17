package de.lenic.redsync.serialization.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.lenic.redsync.objects.DataKey;
import de.lenic.redsync.objects.PlayerData;
import de.lenic.redsync.serialization.Serializer;
import org.bukkit.Color;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class PlayerDataSerializer extends TypeAdapter<PlayerData> {

    @Override
    public void write(JsonWriter out, PlayerData value) throws IOException {
        out.beginObject();

        out.name(DataKey.OWNER).value(value.getOwner().toString());
        out.name(DataKey.INVENTORY).value(Serializer.itemStackArrayToBase64(value.getInventory()));
        out.name(DataKey.OFFHAND).value(Serializer.itemStackToBase64(value.getOffHand()));
        out.name(DataKey.ARMOR).value(Serializer.itemStackArrayToBase64(value.getArmor()));
        out.name(DataKey.ENDERCHEST).value(Serializer.itemStackArrayToBase64(value.getEnderchest()));

        out.name(DataKey.POTION_EFFECTS);
        out.beginArray();
        for(PotionEffect effect : value.getPotionEffects()) {
            out.beginObject();
            out.name("type").value(effect.getType().getName());
            out.name("duration").value(effect.getDuration());
            out.name("amplifier").value(effect.getAmplifier());
            out.name("ambient").value(effect.isAmbient());
            out.name("particles").value(effect.hasParticles());
            if(effect.getColor() != null)
                out.name("color").value(effect.getColor().asRGB());
            out.endObject();
        }
        out.endArray();

        out.name(DataKey.MAX_HEALTH).value(value.getMaxHealth());
        out.name(DataKey.HEALTH).value(value.getHealth());
        out.name(DataKey.FOOD).value(value.getFood());
        out.name(DataKey.EXP).value(value.getExp());
        out.name(DataKey.LEVEL).value(value.getLevel());
        out.name(DataKey.GAMEMODE).value(value.getGameMode());
        out.name(DataKey.FIRETICKS).value(value.getFireTicks());
        out.name(DataKey.FALL_DISTANCE).value(value.getFallDistance());
        out.name(DataKey.SELECTED_SLOT).value(value.getSelectedSlot());
        out.name(DataKey.EXHAUSTION).value(value.getExhaustion());
        out.name(DataKey.SATURATION).value(value.getSaturation());

        out.endObject();
    }

    @Override
    public PlayerData read(JsonReader in) throws IOException {
        PlayerData data = new PlayerData();

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case DataKey.OWNER:
                    data.setOwner(UUID.fromString(in.nextString()));
                    break;
                case DataKey.INVENTORY:
                    data.setInventory(Serializer.itemStackArrayFromBase64(in.nextString()));
                    break;
                case DataKey.OFFHAND:
                    data.setOffHand(Serializer.itemStackFromBase64(in.nextString()));
                    break;
                case DataKey.ARMOR:
                    data.setArmor(Serializer.itemStackArrayFromBase64(in.nextString()));
                    break;
                case DataKey.ENDERCHEST:
                    data.setEnderchest(Serializer.itemStackArrayFromBase64(in.nextString()));
                    break;
                case DataKey.POTION_EFFECTS:
                    Collection<PotionEffect> potionEffects = new HashSet<>();
                    PotionEffectType type = null;
                    int duration = 0;
                    int amplifier = 0;
                    boolean ambient = true;
                    boolean particles = true;
                    Color color = Color.AQUA;

                    in.beginArray();
                    while (in.hasNext()) {
                        in.beginObject();
                        while(in.hasNext()) {
                            switch (in.nextName()) {
                                case "type": type = PotionEffectType.getByName(in.nextString()); break;
                                case "duration": duration = in.nextInt(); break;
                                case "amplifier": amplifier = in.nextInt(); break;
                                case "ambient": ambient = in.nextBoolean(); break;
                                case "particles": particles = in.nextBoolean(); break;
                                case "color": color = Color.fromRGB(in.nextInt()); break;
                            }
                        }
                        in.endObject();
                        potionEffects.add(new PotionEffect(type, duration, amplifier, ambient, particles, color));
                    }
                    in.endArray();
                    data.setPotionEffects(potionEffects);
                    break;
                case DataKey.MAX_HEALTH:
                    data.setMaxHealth(in.nextDouble());
                    break;
                case DataKey.HEALTH:
                    data.setHealth(in.nextDouble());
                    break;
                case DataKey.FOOD:
                    data.setFood(in.nextInt());
                    break;
                case DataKey.EXP:
                    data.setExp((float) in.nextDouble());
                    break;
                case DataKey.LEVEL:
                    data.setLevel(in.nextInt());
                    break;
                case DataKey.GAMEMODE:
                    data.setGameMode(in.nextInt());
                    break;
                case DataKey.FIRETICKS:
                    data.setFireTicks(in.nextInt());
                    break;
                case DataKey.FALL_DISTANCE:
                    data.setFallDistance((float) in.nextDouble());
                    break;
                case DataKey.SELECTED_SLOT:
                    data.setSelectedSlot(in.nextInt());
                    break;
                case DataKey.EXHAUSTION:
                    data.setExhaustion((float) in.nextDouble());
                    break;
                case DataKey.SATURATION:
                    data.setSaturation((float) in.nextDouble());
                    break;
                case DataKey.EMPTY:
                    data.setEmpty(in.nextBoolean());
            }
        }
        in.endObject();

        return data;
    }

}
