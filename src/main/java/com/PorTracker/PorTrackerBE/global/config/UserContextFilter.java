// package com.PorTracker.PorTrackerBE.global.config;

// import com.PorTracker.PorTrackerBE.global.common.UserContextHolder;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import java.io.IOException;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// @Component
// public class UserContextFilter extends OncePerRequestFilter {
//     @Override
//     protected void doFilterInternal(
//             HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//             throws ServletException, IOException {
//         String userId = request.getHeader("X-USER-ID");
//         if (userId != null) {
//             UserContextHolder.setUserId(userId);
//         }
//         try {
//             filterChain.doFilter(request, response);
//         } finally {
//             UserContextHolder.clear();
//         }
//     }
// }
