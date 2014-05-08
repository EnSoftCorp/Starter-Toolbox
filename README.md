Starter-Toolbox
===============

The Starter-Toolbox is a simple starter framework for creating an analysis toolbox using Atlas.

# Setup

1) Install Atlas.  See [http://www.ensoftcorp.com/atlas](http://www.ensoftcorp.com/atlas/).

2) Install dependencies.  The Starter-Toolbox has a dependency on the `org.apache.commons.io` library.  Install the Apache commons library as a plugin from the [http://www.eclipse.org/orbit/](http://www.eclipse.org/orbit/) repos by navigating to `Help`->`Install New Software...` and entering "[http://download.eclipse.org/tools/orbit/downloads/drops/R20130827064939/repository/](http://download.eclipse.org/tools/orbit/downloads/drops/R20130827064939/repository/)" in the `Work with:` field.  Expand the `All Orbit Bundles` category and select `Apache Commons IO` and then press `Next` and `Finish`.  You will need to restart Eclipse.

Note: If your toolbox project has other dependencies you may need to install additional plugins at this time.

3) Clone the Toobox-Starter repository.

`git clone https://github.com/EnSoftCorp/Starter-Toolbox.git`

4) Import the `com.ensoftcorp.toolbox.analysis` and `com.ensoftcorp.toolbox.interpreter` projects into your Eclipse workspace.

# Using the Toolbox

## Interpreter Project

To use the analysis toolbox interactively make sure you have both the `com.ensoftcorp.toolbox.analysis` and `com.ensoftcorp.toolbox.interpreter` projects imported into the Eclipse workspace.  Then navigate to `Window`->`Show View`->`Other`->`Atlas`->`Atlas Interpreter`.  Select the `com.ensoftcorp.toolbox.interpreter` project from the interpreters list and press `OK`.

From the Interpeter you can run any Java scripts in the `com.ensoftcorp.toolbox.analysis` project.  To automatically import packages or classes on the Interpreter edit the `jatlasInit.scala` file.

To open an interactive Smart View right click on the `com.ensoftcorp.toolbox.interpreter` project and navigate to `Atlas`->`Open Atlas Smart View`.  Drag the Smart View window to your preferred location in the Eclipse IDE.  In the Smart View window click on the down arror and navigate to `Script` and then select the Smart View you'd like to display.

## Headless Mode

The `com.ensoftcorp.toolbox.analysis` project is also an Eclipse plugin that can be installed and run in a headless mode.  To install the Eclipse plugin from the workspace right click on the project and navigate to `Export`->`Plug-in Development`->`Deployable plug-ins and fragments`.  Select `Next` and make sure only the `com.ensoftcorp.toolbox.analysis` project is selected.  Then select the `Install into host.` radio and click `Finish`.  You will need to restart Eclipse.

To run the analysis toolbox project in a headless mode invoke Eclipse from the command line with arguments similiar to the following:

    ./eclipse -application com.ensoftcorp.toolbox.analysis.Headless 
              -nosplash 
              -consoleLog  
              -data <workspace path>/headless-workspace/ 
              -import <project path>/{MyProject | MyApp.apk}
              -output <output path>/output.xml
              -remove-imported-projects-after-analysis
              -vmargs -Dsdtcore.headless=true
              
TODO: Explain arguments

For additional Eclipse runtime arguments see [help.eclipse.org](http://help.eclipse.org/juno/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fmisc%2Fruntime-options.html).
# Extending the Toolbox

## Adding an analysis script
TODO

## Adding a Smart View
TODO

## Query Addons
TODO
