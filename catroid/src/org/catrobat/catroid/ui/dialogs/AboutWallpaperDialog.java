/**
 *  Catroid: An on-device graphical programming language for Android devices
 *  Copyright (C) 2010-2011 The Catroid Team
 *  (<http://code.google.com/p/catroid/wiki/Credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://www.catroid.org/catroid_license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *   
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.ui.dialogs;

import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.livewallpaper.R;
import org.catrobat.catroid.livewallpaper.WallpaperHelper;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.Window;
import android.widget.TextView;

public class AboutWallpaperDialog extends Dialog {

	private Context context;

	public AboutWallpaperDialog(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.dialog_about_wallpaper);
		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_dialog_info);

		setTitle(context.getResources().getString(R.string.lwp_about_wallpaper));
		setCanceledOnTouchOutside(true);

		TextView projectInformationTextView = (TextView) findViewById(R.id.dialog_project_information_text_view);
		projectInformationTextView.setText(getProjectInformationText());
		Linkify.addLinks(projectInformationTextView, Linkify.ALL);

	}

	public String getProjectInformationText() {
		Resources resources = context.getResources();
		Project project = WallpaperHelper.getInstance().getProject();

		String text = resources.getString(R.string.lwp_project_name) + " " + project.getName() + "\n";

		text += resources.getString(R.string.lwp_project_licnese) + " "
				+ resources.getString(R.string.lwp_license_link) + "\n";

		if (project.getDescription() != null) {
			text += resources.getString(R.string.lwp_project_description) + " " + project.getDescription();
		}

		return text;
	}
}