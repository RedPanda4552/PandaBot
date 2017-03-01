/**
 * This file is part of PandaBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2017 Brian Wood
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.redpanda4552.PandaBot.operations;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.redpanda4552.PandaBot.Main;
import io.github.redpanda4552.PandaBot.PandaBot;

public abstract class AbstractOperation {

    protected final Logger log = Main.log;
    
    protected PandaBot pandaBot;
    
    public AbstractOperation(PandaBot pandaBot) {
        this.pandaBot = pandaBot;
    }
    
    /**
     * Execute this AbstractOperation's payload.<br>
     * Implementations of this method should call {@link #complete()} at the end
     * of a successful execution.
     * <b>This method is not defined by the AbstractOperation supertype and must
     *  be defined in each subtype.</b>
     */
    public abstract void execute();
    
    public void complete() {
        pandaBot.addToHistory("Executed " + this.getClass().getSimpleName(), Level.INFO);
    }
    
    public void completeWithError(String str) {
        pandaBot.addToHistory("Executed " + this.getClass().getSimpleName() + " but ran into problem: " + str, Level.WARNING);
    }
}
