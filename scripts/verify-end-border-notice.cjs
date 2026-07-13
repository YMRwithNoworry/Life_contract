const fs = require("node:fs");
const path = require("node:path");

const root = path.resolve(__dirname, "..");

function read(relativePath) {
  return fs.readFileSync(path.join(root, relativePath), "utf8");
}

function requireText(source, text, label) {
  if (!source.includes(text)) {
    throw new Error(`${label}: missing ${text}`);
  }
}

const mixinConfig = read("src/main/resources/life_contract.mixins.json");
const mixinPlugin = read(
  "src/main/java/org/alku/life_contract/mixin/LifeContractMixinPlugin.java",
);
const borderWeaverMixin = read(
  "src/main/java/org/alku/life_contract/mixin/BorderWeaverEventHandlerMixin.java",
);
const modsToml = read("src/main/resources/META-INF/mods.toml");

requireText(
  mixinConfig,
  '"BorderWeaverEventHandlerMixin"',
  "Border Weaver compatibility registration",
);
requireText(
  mixinPlugin,
  "BorderWeaverEventHandlerMixin",
  "optional Border Weaver mixin guard",
);
requireText(
  borderWeaverMixin,
  'targets = "org.alku.border_weaver.handler.BorderEventHandler"',
  "Border Weaver event handler target",
);
requireText(borderWeaverMixin, "@Pseudo", "optional target support");
requireText(
  borderWeaverMixin,
  'target = "Lorg/alku/border_weaver/handler/CountdownHandler;isTaskActive()Z"',
  "countdown notice injection point",
);
requireText(borderWeaverMixin, "ordinal = 1", "distance notice branch selection");
requireText(borderWeaverMixin, "cancellable = true", "notice cancellation");
requireText(borderWeaverMixin, "Level.END.equals", "End-only compatibility guard");
requireText(borderWeaverMixin, "callback.cancel()", "End notice suppression");
requireText(modsToml, 'modId="border_weaver"', "Border Weaver dependency id");

console.log("End border notice verification passed.");
