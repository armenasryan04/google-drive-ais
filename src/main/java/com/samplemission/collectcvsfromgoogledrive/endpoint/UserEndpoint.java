package com.samplemission.collectcvsfromgoogledrive.endpoint;

import com.samplemission.collectcvsfromgoogledrive.model.dto.user.ResponsibleHrForRegistrationDto;
import com.samplemission.collectcvsfromgoogledrive.model.dto.user.UserAuthRequestDto;
import com.samplemission.collectcvsfromgoogledrive.model.dto.user.UserAuthResponseDto;
import com.samplemission.collectcvsfromgoogledrive.model.entity.ResponsibleHr;
import com.samplemission.collectcvsfromgoogledrive.model.mapper.UserMapper;
import com.samplemission.collectcvsfromgoogledrive.repository.ResponsibleHrRepository;
import com.samplemission.collectcvsfromgoogledrive.utill.JwtTokenUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserEndpoint {
  private final ResponsibleHrRepository responsibleHrRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenUtil tokenUtil;
  private final UserMapper userMapper;

  @PostMapping("/auth")
  public ResponseEntity<UserAuthResponseDto> auth(@RequestBody UserAuthRequestDto userDto) {
    Optional<ResponsibleHr> user =
        responsibleHrRepository.findByUsernameOrEmail(userDto.getUsername(), userDto.getUsername());
    if (user.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    if (!passwordEncoder.matches(userDto.getPassword(), user.get().getPassword())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String token = tokenUtil.generateToken(userDto.getUsername());
    return ResponseEntity.ok(new UserAuthResponseDto(token));
  }

  @PostMapping("/register")
  public ResponseEntity<ResponsibleHrForRegistrationDto> register(
      @RequestBody ResponsibleHrForRegistrationDto responsibleHrDto) {
    Optional<ResponsibleHr> byEmail =
        responsibleHrRepository.findByUsernameOrEmail(
            responsibleHrDto.getUsername(), responsibleHrDto.getEmail());
    if (byEmail.isPresent()) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
    ResponsibleHr user = userMapper.toResponsibleHr(responsibleHrDto);
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user = responsibleHrRepository.save(user);
    return ResponseEntity.ok(userMapper.toResponsibleHrDto(user));
  }
}
