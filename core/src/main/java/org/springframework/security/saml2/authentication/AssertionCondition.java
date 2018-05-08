/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.springframework.security.saml2.authentication;

import org.joda.time.DateTime;

public abstract class AssertionCondition<T extends AssertionCondition, EvaluationCritera> {

    private boolean valid = false;
    private boolean evaluated = false;
    private DateTime evaluationTime = null;
    private EvaluationCritera evaluationCriteria = null;

    @SuppressWarnings("checked")
    protected T _this() {
        return (T)this;
    }

    public boolean evaluate(EvaluationCritera evaluationCriteria) {
        this.valid = internalEvaluate(evaluationCriteria);
        this.evaluated = true;
        this.evaluationCriteria = evaluationCriteria;
        this.evaluationTime = new DateTime(System.currentTimeMillis());
        return valid;
    }

    protected abstract boolean internalEvaluate(EvaluationCritera evaluationCriteria);

    public boolean isValid() {
        return valid;
    }

    public boolean wasEvaluated() {
        return evaluated;
    }

    public DateTime getEvaluationTime() {
        return evaluationTime;
    }

    public EvaluationCritera getEvaluationCriteria() {
        return evaluationCriteria;
    }

}
