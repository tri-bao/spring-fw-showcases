/*
 * MIT License
 *
 * Copyright (c) Bao Ho (hotribao@gmail.com)
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
package xyz.codeityourself.springshowcases.jpa.entity;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * @author Bao Ho (hotribao@gmail.com)
 * @since 03.05.2019
 */
@MappedSuperclass
public abstract class AbstractEntity {

    @Transient
    private boolean transientHashCodeLeaked = false;

    @Transient
    private String rawClassName = getClass().getName();

    public abstract Integer getId();

    public abstract void setId(Integer id);

    public boolean isPersisted() {
        return getId() != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj instanceof AbstractEntity) {
            final AbstractEntity other = (AbstractEntity) obj;
            if (isPersisted() && other.isPersisted()) { // both entities are not new

                // Because entities are currently used in client side, we cannot use HibernateProxyHelper here >
                // we cannot compare class for sure they are the same class, just compare ID.
                return getId().equals(other.getId()) && rawClassName.equals(other.rawClassName);
            }
            // if one of entity is new (transient), they are considered not equal.
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (!isPersisted()) { // is new or is in transient state.
            transientHashCodeLeaked = true;
            return -super.hashCode();
        }

        // because hashcode has just been asked for when the object is in transient state
        // at that time, super.hashCode(); is returned. Now for consistency, we return the
        // same value.
        if (transientHashCodeLeaked) {
            return -super.hashCode();
        }
        return getId().hashCode();
        // The above mechanism obey the rule: if 2 objects are equal, their hashcode must be same.
    }

    @Override
    public String toString() {
        return String.format("[%s: #%s]", getClass().getSimpleName(), getId());
    }
}
