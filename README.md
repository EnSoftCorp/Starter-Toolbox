Starter-Toolbox
===============

The Starter-Toolbox is a simple starter framework for creating an analysis toolbox using Atlas.

# Setup

### 1) Install Atlas.  See [http://www.ensoftcorp.com/atlas](http://www.ensoftcorp.com/atlas/).

### 2) Install dependencies.  

The Starter-Toolbox has a dependency on the [Toolbox-Commons](https://github.com/EnSoftCorp/toolbox-commons) library.  It can be installed by following instructions at [https://ensoftcorp.github.io/toolbox-commons/install.html](https://ensoftcorp.github.io/toolbox-commons/install.html).

The Starter-Toolbox also has a dependency on the `org.apache.commons.codec` library.  Install the Apache commons library as a plugin from the [http://www.eclipse.org/orbit/](http://www.eclipse.org/orbit/) repos by navigating to `Help`->`Install New Software...` and entering "[http://download.eclipse.org/tools/orbit/downloads/drops/R20140525021250/repository/](http://download.eclipse.org/tools/orbit/downloads/drops/R20140525021250/repository/)" in the `Work with:` field.  Expand the `All Orbit Bundles` category and select `Apache Commons Codec` (version 1.4.0) and then press `Next` and `Finish`.  You will need to restart Eclipse.

Note: If your toolbox project has other dependencies you may need to install additional plugins at this time.

### 3) Fork or clone the Toobox-Starter repository.

`git clone https://github.com/EnSoftCorp/Starter-Toolbox.git`

4) Import the `toolbox.analysis` and `toolbox.shell` projects into your Eclipse workspace.

# Using the Toolbox

## Shell Project

To use the analysis toolbox interactively make sure you have both the `toolbox.analysis` and `toolbox.shell` projects imported into the Eclipse workspace.  Then navigate to `Window`->`Show View`->`Other`->`Atlas`->`Atlas Shell`.  Select the `toolbox.shell` project from the shells list and press `OK`.

From the Atlas Shell you can run any program analyzers in the `toolbox.analysis` project.  To automatically import packages or classes on the Shell edit the `shellInit.scala` file.

To open an interactive Smart View right click on the `toolbox.shell` project and navigate to `Atlas`->`Open Atlas Smart View`.  Drag the Smart View window to your preferred location in the Eclipse IDE.  In the Smart View window click on the down arrow and navigate to `Script` and then select the Smart View you'd like to display.

## Headless Mode

The `toolbox.analysis` project is also an Eclipse plugin that can be installed and run in a headless mode.  To install the Eclipse plugin from the workspace right click on the project and navigate to `Export`->`Plug-in Development`->`Deployable plug-ins and fragments`.  Select `Next` and make sure only the `toolbox.analysis` project is selected.  Then select the `Install into host.` radio and click `Finish`.  You will need to restart Eclipse.

To run the analysis toolbox project in a headless mode invoke Eclipse from the command line with arguments similar to the following:

    ./eclipse -application toolbox.analysis.Headless 
              -nosplash 
              -consoleLog  
              -data <workspace path>/headless-workspace/ 
              -import <project path>/{MyProject}
              -output <output path>/output.xml
              -remove-imported-projects-after-analysis
              -vmargs -Dsdtcore.headless=true
              
### Eclipse Arguments Explained

| **Argument**                                              |                                                **Explanation**                                                |
|-----------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| -application toolbox.analysis.Headless                    | The identifier of the Eclipse application to run. This specifies the headless toolbox entry point.            |
| -nosplash                                                 | Disables the Eclipse splash screen                                                                            |
| -consoleLog                                               | Redirects any log output sent to Java's System.out (typically back to the command shell if any)               |
| -data &lt;workspace path&gt;                              | Set the Eclipse workspace to use                                                                              |
| -vmargs -Dsdtcore.headless=true                           | Sets a VM argument to run the Scala plugin in a headless mode.  Without this argument the toolbox will crash. |

For additional Eclipse runtime arguments see [help.eclipse.org](http://help.eclipse.org/juno/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fmisc%2Fruntime-options.html).

### Headless Toolbox Arguments Explained

| **Argument**                                          |                            **Explanation**                             |
|-------------------------------------------------------|------------------------------------------------------------------------|
| -import &lt;project path&gt;/{MyProject or MyApp.apk} | Imports a Eclipse project or Android binary into the workspace    |
| -sdk-bundle-path &lt;Android SDK Bundle&gt;           | Manually specify the Android SDK Bundle path, if not specified the toolbox will search Eclipse preferences for this information. |
| -output &lt;output file path&gt;                | Sets the output file path                                              |
| -close-imported-projects-after-analysis         | Closes the imported project after the analysis is complete             |
| -remove-imported-projects-after-analysis        | Closes and removes the imported project after the analysis is complete |

# Extending the Toolbox

## Adding an Analyzer
To add a program analyzer extend `com.ensoftcorp.open.toolbox.commons.analysis.Analyzer` and implement the `evaluateEnvelope` method.  Alternatively create your own Java classes for analysis and invoke them directly from the Headless entry point or on the Atlas Shell.

## Adding a Smart View
To add a custom Atlas Smart View implement the `AtlasSmartViewScript` interface and enable the Class in the `selectionScripts.atlas` file.  The custom Smart View can be accessed by right clicking on the `toolbox.shell` project, and selecting `Atlas`->`Open Atlas Smart View`.  Drag the Smart View window to your preferred location in the Eclipse IDE.  In the Smart View window click on the down arrow and navigate to `Script` and then select the Smart View you'd like to display.

An example custom Smart View can be seen in `toolbox.shell.smartviews.SuperTypeHierarchy`.

## Query Addons
QueryAddons is a collection of Scala methods that are dynamically added to the Q query object.  By adding methods here you can add custom query chaining methods.  

Some examples are present in `toolbox.shell.addons.QueryAddons.scala`.

## Adding Toolbox Dependencies
Depending on your analysis needs, other specialized toolboxes exist that may satisfy or quick start your analysis task.  Some toolboxes compatible with this starter toolbox are listed below.

- [Android Essentials Toolbox](https://github.com/EnSoftCorp/android-essentials-toolbox) - Utilities for analyzing Android permissions and the Android Manifest
- [AuditMon](https://github.com/EnSoftCorp/AuditMon) - Utilities for recording analyst code audits
