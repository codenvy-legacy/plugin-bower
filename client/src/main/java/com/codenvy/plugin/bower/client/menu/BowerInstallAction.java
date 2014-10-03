/*******************************************************************************
 * Copyright (c) 2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.bower.client.menu;

import com.codenvy.api.analytics.logger.AnalyticsEventLogger;
import com.codenvy.api.builder.BuildStatus;
import com.codenvy.api.builder.dto.BuildOptions;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.event.RefreshProjectTreeEvent;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.plugin.bower.client.BowerResources;
import com.codenvy.plugin.bower.client.builder.BuildFinishedCallback;
import com.codenvy.plugin.bower.client.builder.BuilderAgent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import java.util.Arrays;
import java.util.List;

/**
 * Action that install bower dependencies.
 * @author Florent Benoit
 */
public class BowerInstallAction extends CustomAction implements BuildFinishedCallback {

    private DtoFactory dtoFactory;

    private BuilderAgent builderAgent;

    private EventBus eventBus;

    private boolean buildInProgress;

    private final AnalyticsEventLogger analyticsEventLogger;
    private       AppContext           appContext;

    @Inject
    public BowerInstallAction(LocalizationConstant localizationConstant,
                              DtoFactory dtoFactory,
                              BuilderAgent builderAgent,
                              AppContext appContext,
                              EventBus eventBus,
                              BowerResources bowerResources,
                              AnalyticsEventLogger analyticsEventLogger) {
        super(appContext, localizationConstant.bowerInstallText(), localizationConstant.bowerInstallDescription(), bowerResources.buildIcon());
        this.dtoFactory = dtoFactory;
        this.builderAgent = builderAgent;
        this.appContext = appContext;
        this.analyticsEventLogger = analyticsEventLogger;
        this.eventBus = eventBus;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        analyticsEventLogger.log(this);
        installDependencies();
    }


    public void installDependencies() {
        buildInProgress = true;
        List<String> targets = Arrays.asList("install");
        BuildOptions buildOptions = dtoFactory.createDto(BuildOptions.class).withTargets(targets).withBuilderName("bower");
        builderAgent.build(buildOptions, "Installation of Bower dependencies...", "Bower dependencies successfully downloaded",
                           "Bower install failed", "bower", this);
    }

    @Override
    public void onFinished(BuildStatus buildStatus) {
        // and refresh the tree if success
        if (buildStatus == BuildStatus.SUCCESSFUL) {
            eventBus.fireEvent(new RefreshProjectTreeEvent());
        }
        buildInProgress = false;
        appContext.getCurrentProject().setIsRunningEnabled(true);
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabled(!buildInProgress);
    }

}
