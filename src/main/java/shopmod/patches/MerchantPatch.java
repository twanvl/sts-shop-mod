package shopmod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.shop.Merchant;

import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

@SpirePatch(clz = Merchant.class, method = "render")
public class MerchantPatch {
    public static ExprEditor Instrument () {
        return new ExprEditor() {
            @Override
            public void edit(FieldAccess f) throws CannotCompileException {
                if (f.getFieldName().equals("MERCHANT_RUG_IMG")) {
                    f.replace(" $_ = shopmod.relics.MerchantsRug.getMerchantRugImage(); ");
                }
            }
        };
    }
}