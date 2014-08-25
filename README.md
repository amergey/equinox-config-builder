equinox-config-builder
======================

Simple utility allowing to generate config.ini file from an "eclipse" directory.

Build status
------------

[![Build Status](https://api.travis-ci.org/sarod/equinox-config-builder.png)](https://travis-ci.org/sarod/equinox-config-builder)


Usage
-----

	java -jar equinox-config-builder-<version>.jar <eclipseDirectory> [<defaultStartLevel> [<bundleStartLevelsPropertyFile>]]

eclipseDirectory: the eclipse directory that should contains a plugins subdirectory and where configuration/config.ini will be generated.
defaultStartLevel: the value to use for osgi.bundles.defaultStartLevel. When not specified defaults to 4
bundleStartLevelsPropertyFile: a property file to specify start level for bundles that should not use defaultStartLevel. The property file should use bundle symbolic name as key and start level as value e.g. org.eclipse.equinox.common=2
 
