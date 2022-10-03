# -*- codeing = utf-8 -*-
# @Time : 2022/10/3 16:30
# @Author : llcc
# @File : duipai.py
# @Software : PyCharm


# 生成testfile.txt文件
import os
import re
import time

mainPath = "E:\\a_class\\2022_compiler\\experiment\\2022testfiles0927"
point = 0
correct = True

for path, dirs, files in os.walk(mainPath):
    print(path)
    # print(dirs)
    # print(files)
    # print("before open files:" + os.getcwd())
    for file in files:
        os.chdir(path)
        # print("filepath:" + os.getcwd())
        with open(file, "r", encoding='utf-8') as f:
            # matchObj1 = re.match("input.*", f.name)
            matchObj2 = re.match("testfile.*", f.name)

            if matchObj2:
                print(f.name)
                data = f.readlines()
                os.chdir(mainPath)
                os.chdir("..")

                with open("testfile.txt", "w", encoding='utf-8') as dst:
                    dst.writelines(data)

                # 开始测试
                print("start testing", point)
                os.system("java -jar Compiler2021_dhy.jar ")
                with open("output.txt", "r") as rsc:
                    ans = rsc.readlines()
                with open("answer.txt", "w") as a:
                    a.writelines(ans)
                start = time.perf_counter()
                os.system("java -jar compiler.jar ")
                with open("output.txt", "r") as my:
                    res = my.readlines()
                with open("result.txt", "w") as a:
                    a.writelines(res)
                print("start judging", point)
                if len(res) != len(ans):
                    print("WA!")
                    correct = False
                    break
                else:
                    for i in range(len(ans)):
                        if res[i] != ans[i]:
                            print("WA!")
                            correct = False
                            break
                point += 1
                if not correct:
                    break
    if not correct:
        break
    os.chdir(mainPath)
    # print(os.getcwd())

# filepathFullA = '2022testfiles0927\\full\\A'
# filepathFullB = '/2022testfiles0927/full/B'
# filepathFullC = '/2022testfiles0927/full/C'
#
# fileList = os.listdir(filepathFullA.replace("\\", "/"))
# print(fileList)
# print(os.getcwd())
# for file in fileList:
#     # os.chdir(filepathFullA)
#     with open(file,"r") as f:
#         matchObj1 = re.match("input.*", f.name)
#         matchObj2 = re.match("testfile.*", f.name)
#         if matchObj1 or matchObj2:
#             data = f.readlines()
#             # with open("testfile.txt", "w") as dst:
#             #     dst.writelines(data)
