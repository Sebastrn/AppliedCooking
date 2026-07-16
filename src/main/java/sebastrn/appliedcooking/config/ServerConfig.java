package sebastrn.appliedcooking.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    private final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    private final ModConfigSpec spec;

    private final KitchenStation kitchenStation;

    public ServerConfig() {
        kitchenStation = new KitchenStation();

        spec = builder.build();
    }

    public ModConfigSpec getSpec() {
        return spec;
    }

    public KitchenStation getKitchenStation() {
        return kitchenStation;
    }

    public class KitchenStation {
        private final ModConfigSpec.DoubleValue idlePowerDrain;

        public KitchenStation() {
            builder.push("kitchenStation");

            idlePowerDrain = builder
                    .comment("The power the Kitchen Station draws from the linked ME network each tick while connected (AE/t).",
                            "It is paid every tick, and only if the network can cover it in full — a network that can't keep up",
                            "leaves the Station offline until power returns. Set to 0 to make the Station free to run.")
                    .defineInRange("idlePowerDrain", 5.0, 0.0, Double.MAX_VALUE);

            builder.pop();
        }

        public double getIdlePowerDrain() {
            return idlePowerDrain.get();
        }
    }
}
