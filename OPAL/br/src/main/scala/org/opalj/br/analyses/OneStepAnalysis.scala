/* BSD 2-Clause License:
 * Copyright (c) 2009 - 2017
 * Software Technology Group
 * Department of Computer Science
 * Technische Universität Darmstadt
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.opalj
package br
package analyses

/**
 * An analysis that performs all computations in one step. Only very short-running
 * analyses should use this interface as reporting progress is not supported.
 *
 * @author Michael Eichberg
 */
trait OneStepAnalysis[Source, +AnalysisResult] extends Analysis[Source, AnalysisResult] {

    /*abstract*/ def doAnalyze(
        project:       Project[Source],
        parameters:    Seq[String]     = List.empty,
        isInterrupted: () ⇒ Boolean
    ): AnalysisResult

    override final def analyze(
        project:                Project[Source],
        parameters:             Seq[String]                = List.empty,
        initProgressManagement: (Int) ⇒ ProgressManagement = ProgressManagement.None
    ): AnalysisResult = {

        val pm = initProgressManagement(1 /* number of steps */ )
        pm.progress(1, ProgressEvents.Start, Some(title))
        var wasKilled = false
        val result = doAnalyze(
            project,
            parameters,
            () ⇒ { wasKilled = pm.isInterrupted(); wasKilled }
        )

        if (wasKilled)
            pm.progress(-1, ProgressEvents.Killed, None)
        else
            pm.progress(1, ProgressEvents.End, None)

        result
    }

}
