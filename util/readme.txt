Module description:
The util module consists of a set of utility class used by other modules 
in the ddieditor framework. 

Legal: 
Copyright 2009 Danish Data Archive (http://www.dda.dk) 
 
For all files within util: 

Util is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Util is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser Public License for more details.

You should have received a copy of the GNU Lesser Public License
along with Util. If not, see <http://www.gnu.org/licenses/>.
    
Module Version:
Current module version is given by the property 'xmljar.version' of the
'build.properties' property file.

Building:
Setup:
The project 'lib-java' holding the XmlBeans jars needs to be checked out from
source version control as weel at: ../[project_home]

Build:
    [util_home] ant compile jar
This generates a jar file in the [lib-java_home]/build directory
