package com.atguigu.gulimall.order.web;

import com.atguigu.common.vo.MemberResVo;
import com.atguigu.gulimall.order.intercept.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderItemVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Controller
public class orderWebController {

    @Autowired
    OrderService orderService;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("/toTrade")
    public String toConfirm(Model model) throws ExecutionException, InterruptedException {
        MemberResVo memberResVo = LoginUserInterceptor.threadLocal.get();
        Long memberResVoId = memberResVo.getId();
        OrderConfirmVo orderConfirmVo= orderService.confirmOrder(memberResVoId);
        model.addAttribute("orderConfirmVo",orderConfirmVo);
        return "confirm";
    }
//    确认订单，用户点击提交订单，把submitVo 传过来
    @GetMapping("/submitOrder")
    public String submitOrder(Model model, OrderSubmitVo orderSubmitVo, RedirectAttributes redirectAttributes){
//        下单，去创建订单，验令牌，验价格，锁库存
//        下单成功来到支付选择页
//        下单失败回到订单确认页重新确认订单信息
//        1.执行下单操作
      SubmitOrderResponseVo submitOrderResponseVo= orderService.submitOrder(orderSubmitVo);

      if(submitOrderResponseVo.getCode()==0){
//          下单成功
          model.addAttribute("submitOrderResp",submitOrderResponseVo);
          return "pay";
      }else{
//          下单失败
//          redirectAttributes.addAttribute();
          return "redirect:http://order.gulimall.com:90/toTrade";
      }



    }


}
