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
package de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc.types.TypesPrinter;
import de.monticore.lang.embeddedmontiarc.helper.SymbolPrinter;
import de.monticore.symboltable.ArtifactScope;
import de.monticore.symboltable.CommonSymbol;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.ScopeSpanningSymbol;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.Splitters;
import de.se_rwth.commons.logging.Log;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Symboltable entry for connectors.
 *
 * @author Arne Haber, Michael von Wenckstern
 */
public class ConnectorSymbol extends CommonSymbol implements ElementInstance {

    /* generated by template symboltable.symbols.KindConstantDeclaration */

    public static final EMAConnectorKind KIND = EMAConnectorKind.INSTANCE;

    /**
     * Source of this connector.
     */
    protected String source;

    /**
     * Target of this connector.
     */
    protected String target;

    protected boolean isConstant = false;

    /**
     * is null if not a constantConnector
     */
    protected ConstantPortSymbol constantPortSymbol = null;

    /**
     * use {@link #builder()}
     */
    protected ConnectorSymbol(String name) {
        super(TypesPrinter.fixTargetName(name), KIND);
    }

    public static ConnectorBuilder builder() {
        return new ConnectorBuilder();
    }

    public void setIsConstantConnector(boolean isConstantConnector) {
        this.isConstant = isConstantConnector;
    }

    public boolean isConstant() {
        return isConstant;
    }

    public void setConstantPortSymbol(ConstantPortSymbol portSymbol) {
        this.constantPortSymbol = portSymbol;
        setIsConstantConnector(true);
    }

    /**
     * NOTE: This method is not supported for ConstantConnectors
     *
     * @return the source
     */
    public String getSource() {

        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    protected PortSymbol getPort(String name, boolean isSourcePortName) {
        if (this.getEnclosingScope() == null) {
            Log.warn("Connector does not belong to a component, cannot resolve port");
            return null;
        }
        if (!this.getEnclosingScope().getSpanningSymbol().isPresent()) {
            Log.warn(
                    "Connector is not embedded in component symbol or expanded component instance symbol, cannot resolve port");
            return null;
        }

        // (1) try to load Component.Port or ExpandedComponentInstance.Port
        String fullSource = Joiners.DOT.join(this.getPackageName(),
                this.getEnclosingScope().getSpanningSymbol().get().getName(), name);
        Optional<PortSymbol> port = this.getEnclosingScope().<PortSymbol>resolve(fullSource,
                PortSymbol.KIND);
        if (port.isPresent()) {
            return port.get();
        }

        if (this.getEnclosingScope().getSpanningSymbol()
                .get() instanceof ExpandedComponentInstanceSymbol) {
            Log.info("Connector is embedded in ExpandedComponentInstanceSymbol", "ConnectorSymbol:");
            Log.info(name, "Looking For:");
            String namePort = name;
            if (namePort.contains("."))
                namePort = namePort.split("\\.")[1];
            PortSymbol portSymbol = connectorSourcePort(
                    (ExpandedComponentInstanceSymbol) this.getEnclosingScope().getSpanningSymbol().get(),
                    this);
            if (!isSourcePortName)
                portSymbol = null;
            if (portSymbol != null)
                Log.info(portSymbol.getName(), "1: Found PortSymbol:");
            if (portSymbol != null && portSymbol.getName().equals(namePort))
                return portSymbol;
            portSymbol = connectorTargetPort(
                    (ExpandedComponentInstanceSymbol) this.getEnclosingScope().getSpanningSymbol().get(),
                    this);
            if (isSourcePortName)
                portSymbol = null;
            if (portSymbol != null)
                Log.info(portSymbol.getName(), "2: Found PortSymbol:");
            if (portSymbol != null && portSymbol.getName().equals(namePort))
                return portSymbol;
        }

        if (!(this.getEnclosingScope().getSpanningSymbol().get() instanceof ComponentSymbol)) {
            Log.warn("Connector is not embedded in component symbol, cannot resolve port");
            return null;
        }
        ComponentSymbol cmp = (ComponentSymbol) this.getEnclosingScope().getSpanningSymbol().get();

        // (2) try to load Component.instance.Port
        Iterator<String> parts = Splitters.DOT.split(name).iterator();
        Log.debug("" + name, "NAME:");
        if (!parts.hasNext()) {
            Log.warn("name of connector's source/target is empty, cannot resolve port");
            return null;
        }
        String instance = parts.next();
        Log.debug("" + instance, "instance");
        if (!parts.hasNext()) {
            Log.warn(
                    "name of connector's source/target does has two parts: instance.port, cannot resolve port");
            return null;
        }
        String instancePort = parts.next();
        Log.debug("" + instancePort, "instancePort");
        Optional<ComponentInstanceSymbol> inst = cmp.getSpannedScope()
                .<ComponentInstanceSymbol>resolve(instance, ComponentInstanceSymbol.KIND);
        if (!inst.isPresent()) {
            Log.warn(String.format("Could not find instance %s in component %s, cannot resolve port",
                    instance, cmp.getFullName()));
            return null;
        }
        port = inst.get().getComponentType().getReferencedSymbol().getSpannedScope()
                .resolve(instancePort, PortSymbol.KIND);
        /* PortSymbol portCS=getEnclosingScope().<PortSymbol>resolve(name,PortSymbol.KIND).get();
         * Log.debug(""+portCS.getName()+" "+portCS.getFullName(),"resolved"); */
        if (port.isPresent()) {
            return port.get();
        }
        Log.debug("No case match for" + name, "cannot resolve port");
        return null;
    }

    /**
     * does not return Optional, since every connector has a port if the model is well-formed
     */
    public PortSymbol getSourcePort() {
        if (isConstant())
            return constantPortSymbol;
        return getPort(this.getSource(), true);
    }

    /**
     * does not return Optional, since every connector has a port if the model is well-formed
     */
    public PortSymbol getTargetPort() {
        return getPort(this.getTarget(), false);
    }

    /**
     * returns the component which defines the connector this is independent from the component to
     * which the source and target ports belong to
     *
     * @return is optional, b/c a connector can belong to a component symbol or to an expanded
     * component instance symbol
     */
    public Optional<ComponentSymbol> getComponent() {
        if (!this.getEnclosingScope().getSpanningSymbol().isPresent()) {
            return Optional.empty();
        }
        if (!(this.getEnclosingScope().getSpanningSymbol().get() instanceof ComponentSymbol)) {
            return Optional.empty();
        }
        return Optional.of((ComponentSymbol) this.getEnclosingScope().getSpanningSymbol().get());
    }

    /**
     * returns the expanded component instance which defines the connector this is independent from
     * the component to which the source and target ports belong to
     *
     * @return is optional, b/c a connector can belong to a component symbol or to an expanded
     * component instance symbol
     */
    public Optional<ExpandedComponentInstanceSymbol> getComponentInstance() {
        if (!this.getEnclosingScope().getSpanningSymbol().isPresent()) {
            return Optional.empty();
        }
        if (!(this.getEnclosingScope().getSpanningSymbol()
                .get() instanceof ExpandedComponentInstanceSymbol)) {
            return Optional.empty();
        }
        return Optional
                .of((ExpandedComponentInstanceSymbol) this.getEnclosingScope().getSpanningSymbol().get());
    }

    /**
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(String target) {

        this.target = TypesPrinter.fixTargetName(target);
    }

    @Override
    public String toString() {
        return SymbolPrinter.printConnector(this);
    }

    @Override
    public String getName() {
        return getTarget();
    }

    public static PortSymbol connectorSourcePort(ExpandedComponentInstanceSymbol inst,
                                                 ConnectorSymbol c) {
        Iterator<String> parts = Splitters.DOT.split(c.getSource()).iterator();
        Optional<String> instance = Optional.empty();
        Optional<String> instancePort;
        Optional<PortSymbol> port;
        if (parts.hasNext()) {
            instance = Optional.of(parts.next());
        }
        if (parts.hasNext()) {
            instancePort = Optional.of(parts.next());
            instance = Optional.of(TypesPrinter.FirstLowerCase(instance.get()));

            ExpandedComponentInstanceSymbol inst2 = inst.getSubComponent(instance.get()).get();
            port = inst2.getSpannedScope().<PortSymbol>resolve(instancePort.get(), PortSymbol.KIND);
        } else {
            instancePort = instance;

            port = inst.getSpannedScope().<PortSymbol>resolve(instancePort.get(), PortSymbol.KIND);
        }

        if (port.isPresent()) {
            return port.get();
        }

        Log.debug("False Source: " + c.getSource() + " in: " + c.getEnclosingScope().getName().get(),
                "ConnectorSymbol");
        Log.error("0xAC012 No source has been set for the connector symbol");
        return null;
    }

    public static PortSymbol connectorTargetPort(ExpandedComponentInstanceSymbol inst,
                                                 ConnectorSymbol c) {
        Iterator<String> parts = Splitters.DOT.split(c.getTarget()).iterator();
        Optional<String> instance = Optional.empty();
        Optional<String> instancePort;
        Optional<PortSymbol> port;
        if (parts.hasNext()) {
            instance = Optional.of(parts.next());
        }
        if (parts.hasNext()) {
            instancePort = Optional.of(parts.next());
            Log.debug(instancePort.toString(), "instancePort ");
            instance = Optional.of(TypesPrinter.FirstLowerCase(instance.get()));
            Log.debug(instance.toString(), "instance");
            Optional<ExpandedComponentInstanceSymbol> instOpt2 = inst.getSubComponent(instance.get());
            if (!instOpt2.isPresent()) {
                System.out.println("No component instance with name " + instance.get() + " is present");
                Log.error("0x1AC013 Target instance is missing");
            }
            ExpandedComponentInstanceSymbol inst2 = instOpt2.get();

            //port = inst2.getSpannedScope().<PortSymbol>resolve(instancePort.get(), PortSymbol.KIND);
            Log.info(instancePort.get(), "Looking for Port");
            port = inst2.getPort(instancePort.get());

            printPossibleErrorMessage(port, inst2, instance, instancePort);
        } else {
            instancePort = instance;

            port = inst.getSpannedScope().<PortSymbol>resolve(instancePort.get(), PortSymbol.KIND);
        }

        if (port.isPresent()) {
            return port.get();
        }

        /*if ( c.getTargetPort() != null) {
            return c.getTargetPort();
        }*/
        System.out.println("False target: " + c.getTarget() + " in: " + c.getEnclosingScope().getName().get());
        System.out.println("This means that there is no port: " + c.getTarget());
        System.out.println("For the Connector:\n" + c.toString());
        if(c.getSourcePort().isConstant()){
            System.out.println("(Notice that the source port called "+c.getSource()+ " is a ConstantPort " +
                    "that was created from a constant expression like 1, 2, true or something similar. It is not a part of the issue)");
        }
        Log.error("0xAC013 No target has been set for the connector symbol");
        return null;
    }

    private static void printPossibleErrorMessage(Optional<PortSymbol> port, ExpandedComponentInstanceSymbol inst2, Optional<String> instance, Optional<String> instancePort) {
        Iterator<PortSymbol> portsIter = inst2.getPorts().iterator();
        if (!port.isPresent()) {
            while (portsIter.hasNext()) {
                String curName = portsIter.next().getName();
                if (curName.toLowerCase().equals(instancePort.get().toLowerCase())) {
                    System.out.println("Target Port with name " + instancePort.get() + " does not exist in subcomponent " +
                            "instance " + instance.get());
                    System.out.println("However, a port named " + curName + " does exist in subcomponent " + instance.get());

                }

            }
        }
    }

}
