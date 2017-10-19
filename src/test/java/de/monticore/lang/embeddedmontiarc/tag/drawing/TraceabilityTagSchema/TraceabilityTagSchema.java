/**
 *
 *  ******************************************************************************
 *  MontiCAR Modeling Family, www.se-rwth.de
 *  Copyright (c) 2017, Software Engineering Group at RWTH Aachen,
 *  All rights reserved.
 *
 *  This project is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version.
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * *******************************************************************************
 */
/* generated from model null*/
/* generated by template templates.de.monticore.lang.montiarc.tagschema.TagSchema*/


package de.monticore.lang.embeddedmontiarc.tag.drawing.TraceabilityTagSchema;

import de.monticore.CommonModelingLanguage;
import de.monticore.lang.montiarc.tagging._symboltable.TagableModelingLanguage;
import de.monticore.symboltable.resolving.CommonResolvingFilter;

/**
 * generated by TagSchema.ftl
 */
public class TraceabilityTagSchema {

  protected static TraceabilityTagSchema instance = null;

  protected TraceabilityTagSchema() {}

  protected static TraceabilityTagSchema getInstance() {
    if (instance == null) {
      instance = new TraceabilityTagSchema();
    }
    return instance;
  }

  protected void doRegisterTagTypes(TagableModelingLanguage modelingLanguage) {
    // all ModelingLanguage instances are actually instances of CommonModelingLanguage
    if(modelingLanguage instanceof CommonModelingLanguage) {
      CommonModelingLanguage commonModelingLanguage = (CommonModelingLanguage)modelingLanguage;

      modelingLanguage.addTagSymbolCreator(new IsTraceableSymbolCreator());
      commonModelingLanguage.addResolver(CommonResolvingFilter.create(IsTraceableSymbol.KIND));
      modelingLanguage.addTagSymbolCreator(new TraceableSymbolCreator());
      commonModelingLanguage.addResolver(CommonResolvingFilter.create(TraceableSymbol.KIND));
    }
  }

  public static void registerTagTypes(TagableModelingLanguage modelingLanguage) {
    getInstance().doRegisterTagTypes(modelingLanguage);
  }
}