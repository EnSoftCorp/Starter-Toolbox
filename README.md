Starter-Toolbox
===============

The Starter-Toolbox is a simple starter framework for creating an analysis toolbox using Atlas.

# Setup

1) Install Atlas.  See [http://www.ensoftcorp.com/atlas](http://www.ensoftcorp.com/atlas/).

2) Clone the Toobox-Starter repository.

`git clone https://github.com/EnSoftCorp/Starter-Toolbox.git`

3) Import the `com.ensoftcorp.toolbox.analysis` and `com.ensoftcorp.toolbox.interpreter` projects into your Eclipse workspace.

# Using the Toolbox

## Interpreter Project

To use the analysis toolbox interactively make sure you have both the `com.ensoftcorp.toolbox.analysis` and `com.ensoftcorp.toolbox.interpreter` projects imported into the Eclipse workspace.  Then navigate to `Window`->`Show View`->`Other`->`Atlas`->`Atlas Interpreter`.  Select the `com.ensoftcorp.toolbox.interpreter` project from the interpreters list and press `OK`.

From the Interpeter you can run any Java scripts in the `com.ensoftcorp.toolbox.analysis` project.  To automatically import packages or classes on the Interpreter edit the `jatlasInit.scala` file.

To open an interactive Smart View right click on the `com.ensoftcorp.toolbox.interpreter` project and navigate to `Atlas`->`Open Atlas Smart View`.  Drag the Smart View window to your preferred location in the Eclipse IDE.  In the Smart View window click on the down arror and navigate to `Script` and then select the Smart View you'd like to display.

## Headless Mode

The `com.ensoftcorp.toolbox.analysis` project is also an Eclipse plugin that can be installed and run in a headless mode.  To install the Eclipse plugin from the workspace right click on the project and navigate to `Export`->`Plug-in Development`->`Deployable plug-ins and fragments`.  Select `Next` and make sure only the `com.ensoftcorp.toolbox.analysis` project is selected.  Then select the `Install into host.` radio and click `Finish`.  You will need to restart Eclipse.

    ./eclipse -application com.ensoftcorp.toolbox.analysis.Headless 
              -nosplash 
              -consoleLog  
              -data <workspace path>/workspace/ 
              -import <project path>/TestProject 
              -output <output path>/output.xml

# Extending the Toolbox

## Adding an analysis script
TODO

## Adding a Smart View
TODO

## Query Addons
TODO
