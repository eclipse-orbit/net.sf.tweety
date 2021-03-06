/*
 *  This file is part of "Tweety", a collection of Java libraries for
 *  logical aspects of artificial intelligence and knowledge representation.
 *
 *  Tweety is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 3 as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.tweety.agents.gridworldsim.commons;

import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.apache.log4j.Logger;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This error handler gets called if something bad happened during the validation of an XML document.
 * It logs the error and throws an appropriate SAXException.
 * @author Stefan Tittel <bugreports@tittel.net>
 */
public class ValidationErrorHandler extends DefaultHandler {

    private static final Logger logger = Logger.getLogger(ValidationErrorHandler.class);

    /**
     * Gets called when an error occurred during validation.
     * @param exception the error that occurred
     * @throws SAXException the error that occurred
     */
    @Override
    public void error(SAXParseException exception)
            throws SAXException {
        logger.warn("An error occurred validating an XML document: Check your XML!", exception);
        throw (exception);
    }

    /**
     * Gets called when a fatal error occurred during validation.
     * @param exception the fatal error that occurred
     * @throws SAXException the fatal error that occurred
     */
    @Override
    public void fatalError(SAXParseException exception)
            throws SAXException {
        logger.warn("a fatal error occurred validating an XML document: Check your XML!", exception);
        throw (exception);
    }

    /**
     * Gets called when a warning occurred during validation.
     * @param exception the warning that occurred
     */
    @Override
    public void warning(SAXParseException exception) {
        logger.info("a warning occurred validating an XML document", exception);
    }
}
