package com.github.dougbrunos.servico_auditor_dql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class ServicoAuditorDqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicoAuditorDqlApplication.class, args);
	}

}
