package org.ddialliance.ddiftp.util.log;

/*
* Copyright 2011 Danish Data Archive (http://www.dda.dk) 
* 
* This program is free software; you can redistribute it and/or modify it 
* under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation; either Version 3 of the License, or 
* (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful, 
* but WITHOUT ANY WARRANTY; without even the implied warranty of 
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*  
* You should have received a copy of the GNU Lesser General Public 
* License along with this library; if not, write to the 
* Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
* Boston, MA  02110-1301  USA
* The full text of the license is also available on the Internet at 
* http://www.gnu.org/copyleft/lesser.html
*/

public interface Log
{
    boolean isDebugEnabled();
    boolean isErrorEnabled();
    boolean isFatalEnabled();
    boolean isInfoEnabled();
    boolean isTraceEnabled();
    boolean isWarnEnabled();
    void trace(Object obj);
    void trace(Object obj, Throwable throwable);
    void debug(Object obj);
    void debug(Object obj, Throwable throwable);
    void info(Object obj);
    void info(Object obj, Throwable throwable);
    void warn(Object obj);
    void warn(Object obj, Throwable throwable);
    void error(Object obj);
    void error(Object obj, Throwable throwable);
    void fatal(Object obj);
    void fatal(Object obj, Throwable throwable);
    void log(LogLevel logLevel, Object obj);
    void log(LogLevel logLevel, Object obj, Throwable throwable);
}
