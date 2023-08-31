package es.incidence.core.manager.auto.screen;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Template;

import es.incidence.core.Core;

public class BaseScreen extends Screen {
    protected BaseScreen(@NonNull CarContext carContext) {
        super(carContext);
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        return null;
    }

    public String getString(int stringId)
    {
        String res = Core.getString(stringId);

        if (res == null || res.length() == 0)
        {
            res = getCarContext().getString(stringId);
        }

        return res;
    }

    public String getString(int stringId, String str1)
    {
        String res = Core.getString(stringId, str1);

        if (res == null || res.length() == 0)
        {
            res = getCarContext().getString(stringId, str1);
        }

        return res;
    }
}
