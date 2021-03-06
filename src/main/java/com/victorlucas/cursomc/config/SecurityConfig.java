package com.victorlucas.cursomc.config;

import com.victorlucas.cursomc.security.JWTAuthenticationFilter;
import com.victorlucas.cursomc.security.JWTAuthorizationFilter;
import com.victorlucas.cursomc.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)//Autoriza colocar as pré-autorizações no endpoints.
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private Environment environment;

    @Autowired
    private UserDetailsService userDetailsService; //O Spring vai identificar o UserDetailsServiceImpl automaticamente...

    @Autowired
    private JWTUtil jwtUtil;

    private static final String[] PUBLIC_MATCHERS = {
            "/h2-console/**",
    };

    //Essa lista acima é as rotas liberadas, ou seja, sem autorização.

    private static final String[] PUBLIC_MATCHERS_GET = {
            "/produtos/**",
            "/categorias/**"
    };
    //Essa lista acima é as rotas liberadas apenas para leitura, ou seja, sem autorização.

    private static final String[] PUBLIC_MATCHERS_POST = {
            "/clientes/**",
            "/auth/forgot/**"
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        if (Arrays.asList(environment.getActiveProfiles()).contains("test")){
            http.headers().frameOptions().disable();
        }//Esse cara é para liberar acesso ao h2

        http.cors().and().csrf().disable();
        //cors(), o spring vai pegar como base o métodos corsConfigurationSource feito lá em baixo.
        //.and().csrf().disable(); ele desativa proteção contra csrf.

        http.authorizeRequests()
                .antMatchers(HttpMethod.POST, PUBLIC_MATCHERS_POST).permitAll()
                .antMatchers(HttpMethod.GET, PUBLIC_MATCHERS_GET).permitAll() //apenas o método GET
                .antMatchers(PUBLIC_MATCHERS).permitAll()
                .anyRequest().authenticated();
        // Chama o authorizeRequests, depois chama antMatchers para pegar a lista de vetor,
        // e chama permitAll para todos os caminhos que tiverem na lista de rotas serem liberadas.
        // .anyRequest().authenticated(), ou seja, "para todo o resto, exigir"
        http.addFilter(new JWTAuthenticationFilter(authenticationManager(), jwtUtil));
        http.addFilter(new JWTAuthorizationFilter(authenticationManager(), jwtUtil, userDetailsService));

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        //Para segurar que nosso backend não vai criar sessão de usuário, usando o STATELESS.

        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        http.addFilterBefore(filter, CsrfFilter.class);

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
        //.userDetailsService(userDetailsService()) -> Quem é o userDetailsService ?
        //.passwordEncoder(bCryptPasswordEncoder()) -> Quem é o password encoder?

    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }

    //Esse método é um "padrão", ele dá o acesso básico em múltiplas fontes para todos os caminhos,

    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder (){
        return new BCryptPasswordEncoder();
    }
    //Um método para codificar a senha.

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
        .antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/**", "/swagger-ui.html", "/webjars/**");
    }
}
