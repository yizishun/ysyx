import sys
import pandas as pd
import matplotlib.pyplot as plt

trace1 = sys.argv[1]
trace2 = sys.argv[2]

# 读取CSV文件
df = pd.read_csv(trace1)
df2 = pd.read_csv(trace2)

plt.figure(figsize=(20,8))
# 绘制饼状图（时间）
selected_columns = ['跳转指令占用时间', '加载指令占用时间', '存储指令占用时间','计算指令占用时间','csr指令占用时间','其他指令占用时间']  # 选择特定的列
selected_values = df2[selected_columns].iloc[-1]  # 提取最后一行的这些
plt.subplot(221)
plt.pie(selected_values, labels=['jumpInst', 'loadInst', 'storeInst','calInst','csrInst','otherInst'], autopct='%1.1f%%', textprops={'fontsize': 4})
plt.title("Inst time")
# 绘制折线图
plt.subplot(222)
plt.plot(df['cpuCycle'], df['指令数'], label='Inst')
plt.plot(df['cpuCycle'], df['跳转指令'], label='jumpInst')
plt.plot(df['cpuCycle'], df['加载指令'], label='loadInst')
#plt.plot(df['cpuCycle'], df['存储指令'], label='storeInst')
plt.plot(df['cpuCycle'], df['计算指令'], label='calInst')
plt.title('Inst number trend')
plt.xlabel('cpuCycle')
plt.ylabel('number')
plt.legend()
# 绘制饼转图（数量）
plt.subplot(223)
selected_columns = ['IFU获得指令', 'EXU结束计算', 'LSU获得数据'] 
selected_values = df[selected_columns].iloc[-1]  # 提取最后一行的这些列
plt.pie(selected_values, labels=['IFUGetInst', 'EXUFinCal', 'LSUGetData'], autopct='%1.1f%%')
plt.title("3 Unit event number")
selected_columns = ['跳转指令', '加载指令', '存储指令','计算指令','csr指令','其他指令']  # 选择特定的列
selected_values = df[selected_columns].iloc[-1]  # 提取最后一行的这些
plt.subplot(224)
plt.pie(selected_values, labels=['jumpInst', 'loadInst', 'storeInst','calInst','csrInst','otherInst'], autopct='%1.1f%%', textprops={'fontsize': 10})
plt.title("Inst number")
# 显示图形
plt.suptitle('Performance Event Trace')
plt.tight_layout()
plt.show()