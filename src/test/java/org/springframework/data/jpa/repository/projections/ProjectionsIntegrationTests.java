/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.jpa.repository.projections;

import java.util.Properties;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.sql.DataSource;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.projections.ProjectionsIntegrationTests.Config;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import lombok.Data;

/**
 * @author Jens Schauder
 */
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
public class ProjectionsIntegrationTests {

	@Autowired DummyEntityWithCollectionRepository repository;

	@Test // DATAJPA-1173
	public void projectionDoesNotChangeNumberOfResults() {

	}

	@Data
	@Entity
	static class DummyEntityWithCollection {
		@Id Long id;

		String name;

		@OneToMany(cascade = CascadeType.ALL) Set<SubEntity> subs;

		String otherAttribute;
	}

	@Data
	@Entity
	static class SubEntity {

		@Id Long id;

		String name;

		String otherAttribute;
	}

	interface DummyEntityWithCollectionRepository extends CrudRepository<DummyEntityWithCollection, Long> {}

	@EnableJpaRepositories(considerNestedRepositories = true)
	static class Config {

		@Bean
		DataSource dataSource() {

			return new EmbeddedDatabaseBuilder() //
					.generateUniqueName(true) //
					.setType(EmbeddedDatabaseType.HSQL) //
					.setScriptEncoding("UTF-8") //
					.ignoreFailedDrops(true) //
					.build();
		}

		@Bean
		AbstractEntityManagerFactoryBean sessionFactoryBean(){

			LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
			factoryBean.setDataSource(dataSource());
			factoryBean.setPersistenceXmlLocation("META-INF/simple-persistence.xml");
			factoryBean.setPersistenceUnitName("simple-persistence-unit");
			factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
//			factoryBean.setPersistenceProvider(new HibernatePersistenceProvider());
			String packageName = this.getClass().getPackage().getName();
			System.out.println(packageName);
			factoryBean.setPackagesToScan(packageName);


			Properties properties = new Properties();
			properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
			factoryBean.setJpaProperties(properties);

			return factoryBean;
		}
	}
}
