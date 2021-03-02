package com.victorlucas.cursomc.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    private JWTUtil jwtUtil;

    private UserDetailsService userDetailsService;

    public JWTAuthorizationFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, UserDetailsService userDetailsService) {
        super(authenticationManager);
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String header = response.getHeader("Authorization");//Ele vai armazenar o valor de Authorization no Header na variável.
        if (header != null && header.startsWith("Bearer ")/*Vai verificar se ele começa com o prefixo definido*/){
            UsernamePasswordAuthenticationToken auth = getAuthentication(header.substring(7));
            if (auth != null){
                SecurityContextHolder.getContext().setAuthentication(auth);//Esse cara vai liberar o acesso do filtro.
            }
        }
    }

    //Vai gerar um objeto do tipo UsernamePasswordAuthenticationToken apartir do token.
    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
        if (jwtUtil.tokenValido(token)){
            String username = jwtUtil.getUsername(token);//Vai pegar o cliente apartir do token.
            UserDetails user = userDetailsService.loadUserByUsername(username);//Vai buscar no banco o username.
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        }
        return null;
    }
}
