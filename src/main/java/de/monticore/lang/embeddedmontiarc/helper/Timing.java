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
package de.monticore.lang.embeddedmontiarc.helper;

import de.monticore.lang.embeddedmontiarc.EmbeddedMontiArcConstants;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTComponent;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTElement;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTMontiArcTiming;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Contains available time paradigms. <br>
 * <br>
 * Copyright (c) 2017, MontiCore. All rights reserved.
 *
 * @author Arne Haber, Robert Heim
 */
public enum Timing {

  /**
   * Component is not aware of time progress.
   */
  UNTIMED("untimed"),

  /**
   * Component is aware of time progress and might react to time events.
   */
  INSTANT("instant"),

  /**
   * Timed component that produces a delay (= needs computation time).
   *
   * @since 2.5.0
   */
  DELAYED("delayed"),

  /**
   * Component is aware of time progress and might react to time events. At max one message per
   * time-slice allowed.
   */
  TIME_SYNCHRONOUS("synchronous"),

  /**
   * Time synchronous component with causal behavior, i.e., at least one time frame delayed.
   */
  CAUSAL_SYNCHRONOUS("causal synchronous");

  private final String toString;

  private Timing(String str) {
    this.toString = str;
  }

  /**
   * @param component ast node of the component
   * @return {@link Timing} of the given component, or
   * {@link EmbeddedMontiArcConstants}, if no explicit time paradigm exists.
   */
  public static Timing getBehaviorKind(ASTComponent component) {
    Timing result = EmbeddedMontiArcConstants.DEFAULT_TIME_PARADIGM;
    for (ASTElement elem : component.getBody().getElements()) {
      if (elem instanceof ASTMontiArcTiming) {
        ASTMontiArcTiming casted = (ASTMontiArcTiming) elem;
        if (casted.isDelayed()) {
          result = Timing.DELAYED;
        }
        else if (casted.isInstant()) {
          result = Timing.INSTANT;
        }
        else if (casted.isSync()) {
          result = Timing.TIME_SYNCHRONOUS;
        }
        else if (casted.isCausalsync()) {
          result = Timing.CAUSAL_SYNCHRONOUS;
        }
        else if (casted.isUntimed()) {
          result = Timing.UNTIMED;
        }
        break;
      }
    }
    return result;
  }

  /**
   * @param paradigm String representation of a {@link Timing}. Must not be null.
   * @return {@link Timing} created from the given string.
   * @throws NullPointerException if {@code paradigm == null}
   */
  public static Timing createBehaviorKind(String paradigm) {
    checkNotNull(paradigm);
    Timing timeParadigm = EmbeddedMontiArcConstants.DEFAULT_TIME_PARADIGM;

    if (paradigm.equals(TIME_SYNCHRONOUS.toString())) {
      timeParadigm = TIME_SYNCHRONOUS;
    }
    else if (paradigm.equals(CAUSAL_SYNCHRONOUS.toString())) {
      timeParadigm = CAUSAL_SYNCHRONOUS;
    }
    else if (paradigm.equals(INSTANT.toString())) {
      timeParadigm = INSTANT;
    }
    else if (paradigm.equals(UNTIMED.toString())) {
      timeParadigm = UNTIMED;
    }
    else if (paradigm.equals(DELAYED.toString())) {
      timeParadigm = DELAYED;
    }
    return timeParadigm;
  }

  @Override
  public String toString() {
    return toString;
  }

  /**
   * @return true, if this paradigm is timed or time-synchronous, else false.
   */
  public boolean isTimed() {
    if (this.equals(UNTIMED)) {
      return false;
    }
    else {
      return true;
    }
  }

  /**
   * @return true, if the component produces an initial delay.
   */
  public boolean isDelaying() {
    return this == DELAYED || this == CAUSAL_SYNCHRONOUS;
  }

  public boolean isTimeSynchronous() {
    return this == TIME_SYNCHRONOUS || this == CAUSAL_SYNCHRONOUS;
  }
}
