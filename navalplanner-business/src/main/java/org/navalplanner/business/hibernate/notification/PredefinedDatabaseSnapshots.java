/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.navalplanner.business.hibernate.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.PostConstruct;

import org.navalplanner.business.common.AdHocTransactionService;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Óscar González Fernández
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class PredefinedDatabaseSnapshots {

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private ISnapshotRefresherService snapshotRefresherService;

    private IAutoUpdatedSnapshot<Map<CriterionType, List<Criterion>>> criterionsMap;

    public Map<CriterionType, List<Criterion>> snapshotCriterionsMap() {
        return criterionsMap.getValue();
    }

    @PostConstruct
    @SuppressWarnings("unused")
    private void postConstruct() {
        criterionsMap = snapshot(calculateCriterionsMap(), CriterionType.class,
                Criterion.class);
    }

    private <T> IAutoUpdatedSnapshot<T> snapshot(Callable<T> callable,
            Class<?>... reloadOnChangesOf) {
        return snapshotRefresherService.takeSnapshot(
                callableOnReadOnlyTransaction(callable),
                ReloadOn.onChangeOf(reloadOnChangesOf));
    }

    @SuppressWarnings("unchecked")
    private <T> Callable<T> callableOnReadOnlyTransaction(Callable<T> callable) {
        return AdHocTransactionService.readOnlyProxy(transactionService,
                Callable.class, callable);
    }

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    private Callable<Map<CriterionType, List<Criterion>>> calculateCriterionsMap() {
        return new Callable<Map<CriterionType, List<Criterion>>>() {
            @Override
            public Map<CriterionType, List<Criterion>> call() throws Exception {
                Map<CriterionType, List<Criterion>> result = new HashMap<CriterionType, List<Criterion>>();
                for (CriterionType criterionType : criterionTypeDAO
                        .getCriterionTypes()) {
                    List<Criterion> criterions = new ArrayList<Criterion>(
                            criterionDAO.findByType(criterionType));
                    result.put(criterionType, criterions);
                }
                return result;
            }
        };
    }

}
