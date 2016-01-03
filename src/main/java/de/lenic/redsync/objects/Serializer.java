package de.lenic.redsync.objects;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class Serializer {

    // Serialize ItemStack array
    public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length);

            for (ItemStack item : items)
                dataOutput.writeObject(item);

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Deserialize ItemStack array
    public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
        try {
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            final BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < items.length; i++)
                items[i] = (ItemStack) dataInput.readObject();

            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    // Serialize Collection<PotionEffect>
    public static String potionEffectsToString(Collection<PotionEffect> effects){
        if(effects.size() == 0)
            return "";

        final StringBuilder builder = new StringBuilder("");
        for(PotionEffect effect : effects)
            builder.append(effect.getType().toString() + ':' + effect.getDuration() + ':' + effect.getAmplifier() + ':' + effect.isAmbient() + ';');
        return builder.toString();
    }

    // Deserialize Collection<PotionEffect>
    public static Collection<PotionEffect> potionEffectsFromString(String serialized){
        if(serialized.equals(""))
            return new ArrayList<>();

        final Collection<PotionEffect> effects = new ArrayList<>();
        String[] potionData;
        for(String s : serialized.split(";")){
            potionData = s.split(":");
            if(PotionEffectType.getByName(potionData[0]) != null){
                effects.add(new PotionEffect(
                        PotionEffectType.getByName(potionData[0]),                                  // Type
                        Integer.parseInt(potionData[1]),                                            // Duration
                        Integer.parseInt(potionData[2]),                                            // Amplifier
                        potionData.length == 4 ? Boolean.parseBoolean(potionData[3]) : false        // Ambient
                ));
            }
        }
        return effects;
    }

}
