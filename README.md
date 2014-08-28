equinox-config-builder
======================

Simple utility allowing to generate config.ini file from an "eclipse" directory.

Build status
------------

[![Build Status](https://api.travis-ci.org/sarod/equinox-config-builder.png)](https://travis-ci.org/sarod/equinox-config-builder)
[![Coverage Status](https://img.shields.io/coveralls/sarod/equinox-config-builder.svg)](https://coveralls.io/r/sarod/equinox-config-builder)

Command Line Usage
------------------

	java -jar equinox-config-builder-1.0.1.jar <eclipseDirectory> [<defaultStartLevel> [<bundleStartLevelsPropertyFile>]]

eclipseDirectory: the eclipse directory that should contains a plugins subdirectory and where configuration/config.ini will be generated.
defaultStartLevel: the value to use for osgi.bundles.defaultStartLevel. When not specified defaults to 4
bundleStartLevelsPropertyFile: a property file to specify start level for bundles that should not use defaultStartLevel. The property file should use bundle symbolic name as key and start level as value e.g. org.eclipse.equinox.common=2
 
Ant task usage
---------------

	<taskdef resource="com/sarod/equinox/config/builder/ant/antlib.xml" classpath="equinox-config-builder-1.0.1.jar"/>
	<equinox-config-builder defaultstartlevel="4" eclipsedirectory="${eclipse.dir}">
		<!-- Specify specific start level for some bundle using thir symbolic names -->
		<bundlestartlevel bundlename="com.sarod.bundle1" startlevel="2"/>
	</equinox-config-builder>
	
	
Build from source 
-----------------
	mvn clean install
	
<!--
How to release to maven central
-------------------------------

1. Configure pgp
2. Add the following to your settings.xml:
	
	<servers>
		<server>
			<id>ossrh</id>
			<username>XXX</username>
			<password>XXX</password>
		</server>
	</servers>

	<profiles>
		<profile>
			<id>sign</id>
			<activation>
				<activeByDefault>true</activeByDefault>
			</activation>
			<properties>
				<gpg.passphrase>XXX</gpg.passphrase>
			</properties>
		</profile>
	</profiles>
	
3. TBD-->