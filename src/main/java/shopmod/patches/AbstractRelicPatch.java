package shopmod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class AbstractRelicPatch {
    @SpirePatch(clz = AbstractRelic.class, method = "update")
    public static class Update {
        public static ExprEditor Instrument () {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("changeType")) {
                        m.replace(" if (!(shopmod.relics.MerchantsRug.isSelling() && this.isObtained)) { $_ = $proceed($$); }");
                    }
                }
            };
        }
    }

    @SpirePatch(clz = AbstractRelic.class, method = "updateRelicPopupClick")
    public static class UpdateRelicPopupClick {
        public static ExprEditor Instrument () {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("open")) {
                        m.replace(" if (shopmod.relics.MerchantsRug.isSelling() && this.isObtained) { shopmod.relics.MerchantsRug.relicPopUp.open(this); } else { $_ = $proceed($$); }");
                    }
                }
            };
        }
    }

}