package com.wang.back.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.back.common.Result;
import com.wang.back.entity.Todo;
import com.wang.back.mapper.TodoMapper;
import com.wang.back.common.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/todo")
public class TodoController {

    @Autowired
    private TodoMapper todoMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final long CACHE_BASE_SECONDS = 30 * 60; //基础过期时间
    private static final int CACHE_RANDOM_BOUND = 5 * 60;   //附加的随机过期时间

    // 查询（分页+搜索+缓存+用户隔离）
    @GetMapping("/page")
    public Result<IPage<Todo>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,      //页码，默认第 1 页
            @RequestParam(defaultValue = "10") Integer pageSize,    //每页条数，默认 10
            @RequestParam(required = false) String keyword,         //搜索关键词，可不传
            @RequestParam(required = false) Integer finished) {     //完成状态，可不传

        //拿到当前用户id
        Long userId = UserContext.getUserId();

        //缓存key加上userId，不同用户独立缓存
        String cacheKey = "todo:page:" + userId + ":" + pageNum + ":" + pageSize
                + ":" + keyword + ":" + finished;

        //先查缓存
        @SuppressWarnings("unchecked")  //忽略警告
        IPage<Todo> cachePage = (IPage<Todo>) redisTemplate.opsForValue().get(cacheKey);    //从Redis取出这个key对应的值
        if (cachePage != null) {
            log.info("命中缓存: {}", cacheKey);
            return Result.success(cachePage);
        }

        //查数据库
        log.info("未命中缓存，查数据库: {}", cacheKey);
        Page<Todo> page = new Page<>(pageNum, pageSize);                 //MyBatis-Plus的分页对象
        LambdaQueryWrapper<Todo> wrapper = new LambdaQueryWrapper<>();   //MyBatis-Plus的条件构造器
        wrapper.eq(Todo::getUserId, userId)                              //只查当前用户的数据
                .like(StringUtils.hasText(keyword), Todo::getContent, keyword)
                .eq(finished != null, Todo::getFinished, finished)
                .orderByDesc(Todo::getCreateTime);  //倒序

        IPage<Todo> result = todoMapper.selectPage(page, wrapper);  //执行分页查询，把结果封装成Page对象

        // 写入缓存
        long expire = CACHE_BASE_SECONDS + new Random().nextInt(CACHE_RANDOM_BOUND);    //过期时间
        redisTemplate.opsForValue().set(cacheKey, result, expire, TimeUnit.SECONDS);    //存入Redis

        return Result.success(result);
    }

    //新增
    @PostMapping
    public Result<Void> add(@RequestBody Todo todo) {
        todo.setUserId(UserContext.getUserId());   //设置所属用户
        todo.setFinished(0);
        todoMapper.insert(todo);
        clearCache();   //清缓存，更新页面数据
        return Result.success(null);
    }

    //切换完成状态
    @PutMapping("/{id}")
    public Result<Void> toggle(@PathVariable Long id) {
        Long userId = UserContext.getUserId();

        //查询时也要带userId，防止改到别人的数据
        //根据id和userId精确查到这条Todo
        Todo todo = todoMapper.selectOne(
                new LambdaQueryWrapper<Todo>()
                        .eq(Todo::getId, id)
                        .eq(Todo::getUserId, userId)
        );
        if (todo == null) return Result.error("任务不存在");

        todo.setFinished(todo.getFinished() == 1 ? 0 : 1);
        todoMapper.updateById(todo);
        clearCache();
        return Result.success(null);
    }

    //删除
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();

        //删的时候也要带userId，防止删掉别人的
        todoMapper.delete(
                new LambdaQueryWrapper<Todo>()
                        .eq(Todo::getId, id)
                        .eq(Todo::getUserId, userId)
        );
        clearCache();
        return Result.success(null);
    }

    //按前缀清缓存（只清当前用户的）
    private void clearCache() {
        Long userId = UserContext.getUserId();
        Set<String> keys = redisTemplate.keys("todo:page:" + userId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            System.out.println("清除缓存：" + keys.size() + " 个 key");
        }
    }
}