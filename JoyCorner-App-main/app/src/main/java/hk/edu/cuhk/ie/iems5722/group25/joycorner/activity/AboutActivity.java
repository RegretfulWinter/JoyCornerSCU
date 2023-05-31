package hk.edu.cuhk.ie.iems5722.group25.joycorner.activity;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import hk.edu.cuhk.ie.iems5722.group25.joycorner.R;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.happy)
                .setDescription("This app is the final project of Liu Yaohui\n and Fu Heming, named JoyCorner.")
                .addItem(getProfileElement())
                .addItem(new Element().setTitle("Version 1.0"))
                .addGroup("Connect with us")
                .addEmail("1155166603@link.cuhk.edu.hk", "Contact LIU YAOHUI")
                .addEmail("1155166603@link.cuhk.edu.hk", "Contact FU HEMING")
                .addWebsite("staff.ie.cuhk.edu.hk/~mho/")
                .addGitHub("liuyaohui301")
                .addItem(getCopyRightsElement())
                .create();

        setContentView(aboutPage);
    }

    Element getProfileElement() {
        Element profileElement = new Element();
        final String description = "The purpose of " +
                "this app is to hope that more students will participate in campus activities, get to know more friends, and " +
                "gain happiness and friendship in cuhk.";
        profileElement.setTitle(description);
        profileElement.setGravity(Gravity.LEFT);
        return profileElement;
    }

    Element getCopyRightsElement() {
        Element copyRightsElement = new Element();
        final String copyrights = String.format(getString(R.string.copy_right), Calendar.getInstance().get(Calendar.YEAR));
        copyRightsElement.setTitle(copyrights);
        copyRightsElement.setIconDrawable(R.drawable.about_icon_copy_right);
        copyRightsElement.setAutoApplyIconTint(true);
        copyRightsElement.setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color);
        copyRightsElement.setIconNightTint(android.R.color.white);
        copyRightsElement.setGravity(Gravity.CENTER);
        copyRightsElement.setOnClickListener(v -> Toast.makeText(AboutActivity.this, copyrights, Toast.LENGTH_SHORT).show());
        return copyRightsElement;
    }
}
