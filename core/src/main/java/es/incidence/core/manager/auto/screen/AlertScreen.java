package es.incidence.core.manager.auto.screen;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarColor;
import androidx.car.app.model.MessageTemplate;
import androidx.car.app.model.OnClickListener;
import androidx.car.app.model.Template;

import es.incidence.core.R;

public class AlertScreen extends BaseScreen
{
    private String message;

    protected AlertScreen(@NonNull CarContext carContext, String message)
    {
        super(carContext);
        this.message = message;
    }

    @NonNull
    @Override
    public Template onGetTemplate()
    {
        Action action1 = new Action.Builder()
                .setTitle(getString(R.string.accept))
                .setBackgroundColor(CarColor.RED)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick()
                    {
                        getScreenManager().pop();
                    }
                }).build();

        MessageTemplate template = new MessageTemplate.Builder(message)
                .setTitle(getString(R.string.nombre_app))
                .addAction(action1)
                .build();

        return template;
    }
}
